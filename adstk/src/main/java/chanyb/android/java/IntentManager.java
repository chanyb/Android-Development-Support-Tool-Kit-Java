package chanyb.android.java;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

public class IntentManager {
    private IntentManager() {
    }

    private static IntentManager instance = new IntentManager();

    public static IntentManager getInstance() {
        return instance;
    }

    public Intent getSettingIntent() {
        return new Intent(Settings.ACTION_SETTINGS);
    }

    public Intent getWifiSettingIntent() {
        return new Intent(Settings.ACTION_WIFI_SETTINGS);
    }

    public Intent getDataSettingIntent() {
        return new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
    }

    public Intent getBluetoothSettingIntent() {
        return new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
    }

    public Intent getApplicationSettingIntent() {
        return new Intent(Settings.ACTION_APPLICATION_SETTINGS);
    }

    public Intent getOtherApplicationRunIntent(Context mContext, String packageName) {
        return mContext.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    /**
     * copy Intent's Extras easily
     * @param original intent object that has extra.
     * @param duplicated new intent that will have extra.
     */
    public void copyExtras(Intent original, Intent duplicated) {
        Bundle bundle = original.getExtras();

        for (String key : bundle.keySet()) {
            Object obj = bundle.get(key);
            if (obj instanceof Integer) duplicated.putExtra(key, (int) obj);
            else if (obj instanceof String) duplicated.putExtra(key, (String) obj);
            else if (obj instanceof Byte) duplicated.putExtra(key, (Byte) obj);
            else if (obj instanceof Boolean) duplicated.putExtra(key, (Boolean) obj);
            else if (obj instanceof byte[]) duplicated.putExtra(key, (byte[]) obj);
            else throw new RuntimeException("unexpected value type");
        }

    }
}
