package chanyb.android.java;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureTaskRunner<T> {
    private FutureTask<T> futureTask;
    ArrayList<Callable<T>> taskList;
    public FutureTaskRunner() {
        taskList = new ArrayList<>();
    }

    public void nextTask(Callable<T> callable) {
        taskList.add(callable);
    }

    private Callback callback;

    public interface Callback {
        Object onScan(Object res);
    }

    public void setCallback(Callback _callback) {
        this.callback = _callback;
    }

    public void start() {
        Thread thread = new Thread(() -> {
            for (Callable<T> callable : taskList) {
                futureTask = new FutureTask<>(callable);
                futureTask.run();
                try {
                    if(futureTask.get() == null) {
                        throw new RuntimeException("futureTaskManager - get Fail..");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Log.i("this", "error", e);
                    Thread.currentThread().interrupt();
                    return ;
                }
            }

            if(callback != null) {
                try {
                    callback.onScan(futureTask.get());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}