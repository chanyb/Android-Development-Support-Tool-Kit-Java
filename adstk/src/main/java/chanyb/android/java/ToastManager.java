package chanyb.android.java;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastManager {
    private static ToastManager instance = new ToastManager();
    private Toast toast;

    private ToastManager() {
    }

    public static ToastManager getInstance() {
        return instance;
    }

    /**
     * Can show toast easy.
     *
     * @param msg want to show message
     */
    public void show(String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (GlobalApplcation.currentActivity != null)
                    toast = Toast.makeText(GlobalApplcation.currentActivity, msg, Toast.LENGTH_SHORT);
                else toast = Toast.makeText(GlobalApplcation.getContext(), msg, Toast.LENGTH_SHORT);
                toast.show();
            }
        }, 0);

    }

}
