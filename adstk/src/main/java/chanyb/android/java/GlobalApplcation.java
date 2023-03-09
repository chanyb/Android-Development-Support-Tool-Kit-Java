package chanyb.android.java;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public class GlobalApplcation extends Application {
    public final static int PREFERENCE_INT_DEFAULT = -989899;
    private static volatile GlobalApplcation instance;
    private static String TAG = "activity";
    Display display;
    Point display_size;
    InputMethodManager imm;


    public static GlobalApplcation getContext() {
        return instance;
    }

    private enum State {
        None, Foreground, Background
    }

    private State state;
    private int running;

    public interface Listener {
        void onBecameForeground();

        void onBecameBackground();
    }

    private Listener stateListener;
    public static Activity currentActivity;

    private void getScreenSize(Activity _activity) {
        //1
        //display = _activity.getWindowManager().getDefaultDisplay();  // in

        //2
        WindowManager wm = (WindowManager) _activity.getSystemService(_activity.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        display_size = new Point();
        display.getRealSize(display_size); // or getSize(size)
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        instance = this;
        state = State.None;
        imm = (InputMethodManager) getSystemService(GlobalApplcation.INPUT_METHOD_SERVICE);
    }

    ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            ActivityLifecycleCallbacks.super.onActivityPreCreated(activity, savedInstanceState);
            GlobalApplcation.currentActivity = activity;
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
            getScreenSize(activity);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            Log.i(TAG, "onActivityStarted: " + activity);
            if (++running == 1 && !activity.isChangingConfigurations()) {
                state = State.Foreground;
                if (stateListener != null) stateListener.onBecameForeground();
            }
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            Log.i(TAG, "onActivityResumed: " + activity);
            GlobalApplcation.currentActivity = activity;
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            Log.i(TAG, "onActivityPaused: " + activity);
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            Log.i(TAG, "onActivityStopped: " + activity);
            if (--running == 0 && !activity.isChangingConfigurations()) {
                state = State.Background;
                if (stateListener != null) stateListener.onBecameBackground();
            }
            if (GlobalApplcation.currentActivity != null) {
                if (activity.getClass() == GlobalApplcation.currentActivity.getClass()) {
                    GlobalApplcation.currentActivity = null;
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
            Log.i(TAG, "onActivitySaveInstanceState: " + activity);
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            Log.i(TAG, "onActivityDestroyed: " + activity);
        }
    };

    public boolean isEqual(Activity a, Class b) {
        return (a.getClass().getName().equals(b.getName()));
    }

    public boolean isBackground() {
        return state == State.Background;
    }

    public boolean isForeground() {
        return state == State.Foreground;
    }

    public void addListener(Listener listener) {
        this.stateListener = listener;
    }

    public void removeListsner() {
        stateListener = null;
    }


    public boolean isServiceRunningCheck(Class serviceClass) {
        ActivityManager manager = (ActivityManager) GlobalApplcation.getContext().getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void createNotificationChannel(String channelId, int importance) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, importance); // id, name, importance
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 2000, 1000, 2000});
            channel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = GlobalApplcation.getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public boolean isNotificationChannelEnabled(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = GlobalApplcation.getContext().getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);

            if (channel != null) {
                return true;
            }
            return false;
        }

        return true;
    }

    public void showSoftInput(EditText editText) {
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideSoftInput(EditText editText) {
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public int getHeight() {
        return display_size.y;
    }

    public void cancelAlarm(Class broadCastReceiverClass, int id, String sAction) {
        AlarmManager alarmManager = (AlarmManager) GlobalApplcation.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(GlobalApplcation.getContext(), broadCastReceiverClass);
        if(sAction != null) intent.setAction(sAction);

        PendingIntent alarmPendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmPendingIntent = PendingIntent.getBroadcast(GlobalApplcation.getContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            alarmPendingIntent = PendingIntent.getBroadcast(GlobalApplcation.getContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        alarmManager.cancel(alarmPendingIntent);
    }

    public String saveBitmapToGallery(Bitmap bitmap, String fileName, String fileDesc) {
        return MediaStore.Images.Media.insertImage(GlobalApplcation.getContext().getContentResolver(), bitmap, fileName, fileDesc);
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        Bitmap bitmap = null;
        ExifInterface exif = null;
        DeviceRotationManager.ROTATE_STATE rotateState = null;
        int orientation = PREFERENCE_INT_DEFAULT;
        // exif 읽기
        try {
            exif = new ExifInterface(uri.getPath());
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    // exif 확인 결과 기본 사진은 90도 회전되어있지만, 내가 읽어온 비트맵은 회전되지 않은 상태이므로 90도 회전해주기 위해 아래와같이 설정.
                    rotateState = DeviceRotationManager.ROTATE_STATE.LANDSCAPE_REVERSE;
                    break;
                case 180:
                    rotateState = DeviceRotationManager.ROTATE_STATE.PORTRAIT;
                    break;
                case 270:
                    rotateState = DeviceRotationManager.ROTATE_STATE.PORTRAIT_REVERSE;
                    break;
                case 0:
                    rotateState = DeviceRotationManager.ROTATE_STATE.LANDSCAPE;
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContext().getContentResolver(), uri));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                bitmap = rotateBitmap(rotateState, bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    private Bitmap rotateBitmap(DeviceRotationManager.ROTATE_STATE captureRotate, Bitmap bitmap) {
        Matrix rotateMatrix = new Matrix();

        if (captureRotate == DeviceRotationManager.ROTATE_STATE.LANDSCAPE) rotateMatrix.postRotate(270);
        else if (captureRotate == DeviceRotationManager.ROTATE_STATE.LANDSCAPE_REVERSE) rotateMatrix.postRotate(90);
        else if (captureRotate == DeviceRotationManager.ROTATE_STATE.PORTRAIT) rotateMatrix.postRotate(0);
        else if (captureRotate == DeviceRotationManager.ROTATE_STATE.PORTRAIT_REVERSE) rotateMatrix.postRotate(180);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, false);
    }

    public void deletePhotoInGallery(Uri uri) {
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, //the album it in
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.TITLE
        };

        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if(cursor == null || cursor.getCount() == 0) return ;
        cursor.moveToFirst();

        int dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
        String data = cursor.getString(dataIdx);
        File photo = new File(data);
        if (photo.exists()) {
            photo.delete();
        }
    }

    public boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}