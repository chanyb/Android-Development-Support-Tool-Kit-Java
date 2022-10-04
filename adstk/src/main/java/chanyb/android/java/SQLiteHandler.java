package chanyb.android.java;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SQLiteHandler {

    private final String TAG = "this";
    private final String fileName;
    private final ArrayList<String> tables;
    private final ArrayList<Map<String, String>> columns;

    SQLiteOpenHelper mHelper = null;
    SQLiteDatabase mDB = null;
    int version;

    private SQLiteHandler(Builder builder) {
        this.fileName = builder.fileName;
        this.tables = builder.tables;
        this.columns = builder.columns;
        this.version = builder.version;

        ArrayList<String> sqls = getCreateTableSql(tables, columns);

        mHelper = new SQLiteOpenHelper(GlobalApplcation.getContext(), fileName, null, version) {
            @Override
            public void onCreate(SQLiteDatabase sqLiteDatabase) {
                for (String sql : sqls) {
                    // 테이블 생성
                    try{
                        sqLiteDatabase.execSQL(sql);
                    } catch (Exception e) {
                        Log.i(TAG, "execSQL error", e);
                    }

                    // 업그레이드인지 확인하고(파일확인), 데이터 있으면 넣기

                }
            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
                for (String table : tables) {
                    // 데이터 저장 필요 (파일저장?..)


                    // 데이터 저장 완료한 테이블 삭제
                    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + table);
                }

                onCreate(sqLiteDatabase);
            }
        };
    }

    private ArrayList<String> getCreateTableSql(ArrayList<String> tables, ArrayList<Map<String,String>> columns) {
        ArrayList<String> sqls = new ArrayList<>();
        for (int idx=0; idx<tables.size(); idx++) {
            // each table
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("CREATE TABLE IF NOT EXISTS ");
            sqlBuilder.append(tables.get(idx));
            sqlBuilder.append("(");

            Map<String, String> column = columns.get(idx);
            for (String key : column.keySet()) {
                sqlBuilder.append(key); // key
                sqlBuilder.append(" ");
                sqlBuilder.append(column.get(key)); // key info (ex. INTEGER PRIMARY KEY AUTOINCREMENT)
                sqlBuilder.append(",");
            }
            sqlBuilder.setLength(sqlBuilder.length()-1); // remove last comma
            sqlBuilder.append(");");

            sqls.add(sqlBuilder.toString());
        }
        return sqls;
    }

    public static class Builder {
        // Requirement paramters
        private final String fileName;
        private final int version;

        // optional paramters
        private final ArrayList<String> tables;
        private final ArrayList<Map<String,String>> columns;

        public Builder(String fileName, int version) {
            this.fileName = fileName;
            this.version = version;
            tables = new ArrayList<>();
            columns = new ArrayList<>();
        }

        public Builder addTable(String table, Map<String,String> column) {
            this.tables.add(table);
            this.columns.add(column);
            return this;
        }

        public SQLiteHandler build() {
            return new SQLiteHandler(this);
        }
    }

    public Cursor select(String tableName, String[] columns, String columnForWhereClause, String[] valueForWhereClause, String groupBy, String having, String orderBy, String limit) {
        mDB = mHelper.getReadableDatabase();
        Cursor c = mDB.query(
                tableName,
                columns,
                columnForWhereClause /* column for where clause*/,
                valueForWhereClause/* value for where clause*/,
                groupBy,
                having,
                orderBy,
                limit);
        return c;
    }


    public void insert(String tableName, Serializable serializable) {
        Log.i(TAG, "insert");
        mDB = mHelper.getWritableDatabase();

        ContentValues contentValue = new ContentValues();

        for (Method method : serializable.getClass().getMethods()) {
            if(!method.getDeclaringClass().getName().endsWith("SerializableClass")) continue;

            // Get name of key
            String field = null;
            try{
                field = method.getName().replace("get", "").toLowerCase();
            } catch (NullPointerException e) {
                Log.i(TAG, "insert error", e);
            }

            // Get data of value
            Object value = null;
            try {
                value = method.invoke(serializable);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.i(TAG, "error", e);
            }

            if(value == null) throw new NullPointerException("insert error - getter returned null");

            /* field에 해당하는 column의 type찾기 s */
            Map<String, String> column = null;
            for (int idx=0; idx< tables.size(); idx++) {
                // 해당하는 테이블 idx 찾기
                if (!tables.get(idx).equals(tableName)) continue;

                // column찾기
                column = columns.get(idx);

                if (column == null) throw new NullPointerException("insert error - column is null");

                for (String columnName : column.keySet()) {
                    // columnName == field 인 값 찾기
                    if (!columnName.equals(field)) continue;

                    String sColumnInfo = column.get(columnName);
                    if (sColumnInfo == null)
                        throw new NullPointerException("insert error - sColumnInfo is null");

                    // first index is type of column
                    String sType = sColumnInfo.split(" ").length > 1 ? sColumnInfo.split(" ")[0].toLowerCase() : sColumnInfo.toLowerCase();

                    switch (sType) {
                        case "text":
                            // put pair of key - value
                            contentValue.put(field, (String) value);
                            break;
                        case "integer":
                            contentValue.put(field, (int) value);
                            break;
                        case "boolean":
                            contentValue.put(field, (Boolean) value);
                            break;
                        default:
                            throw new RuntimeException("sType error");
                    }
                    break;
                }
                break;
                /* Key에 해당하는 column의 type찾기 e */
            }
        }

        mDB.insert(tableName, null, contentValue);
    }

    public void delete(String tableName, String whereClause, String[] whereArgs)
    {
        Log.d(TAG, "delete");
        mDB = mHelper.getWritableDatabase();
        mDB.delete(tableName, whereClause, whereArgs);
    }

    public void update(String tableName, ContentValues contentValue, String whereCluase, String[] whereArgs) {
        mDB = mHelper.getWritableDatabase();
        mDB.update(tableName, contentValue, whereCluase, whereArgs);
    }


    public void close() {
        mHelper.close();
    }
}
