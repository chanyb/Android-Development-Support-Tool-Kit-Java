package com.example.smartbox_dup.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class FutureTaskRunner {
    private FutureTask<Boolean> futureTask;
    ArrayList<Callable<Boolean>> taskList;
    public FutureTaskRunner() {
        taskList = new ArrayList<>();
    }

    public void nextTask(Callable<Boolean> callable) {
        taskList.add(callable);
    }

    public void start() {
        new Thread(() -> {
            for (Callable<Boolean> callable : taskList) {
                futureTask = new FutureTask<>(callable);
                futureTask.run();
                try {
                    if(futureTask.get()) {
                        Log.i("this", "futureTaskManager - nextTask");
                    } else {
                        Log.i("this", "futureTaskManager - fail");
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
