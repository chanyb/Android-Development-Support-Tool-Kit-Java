package chanyb.android.java;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GlobalApplcation extends Application {
    private static volatile GlobalApplcation instance;
    private static String TAG = "activity";

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

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        instance = this;
        state = State.None;
    }

    ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

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
}
