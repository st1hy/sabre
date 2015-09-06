package com.github.st1hy.sabre.core.util;

import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;
import java.util.List;

public class UiThreadHandler {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Runnable> taskList = new LinkedList<>();

    public void post(Runnable runnable) {
        synchronized (taskList) {
            handler.post(appendList(runnable));
        }
    }

    public void postDelayed(Runnable runnable, long delayMillis) {
        synchronized (taskList) {
            handler.postDelayed(appendList(runnable), delayMillis);
        }
    }

    private Runnable appendList(Runnable runnable) {
        Runnable innerTask = createInternalTask(runnable);
        taskList.add(innerTask);
        return innerTask;
    }

    private Runnable createInternalTask(final Runnable task) {
        return new Runnable() {
            @Override
            public void run() {
                synchronized (taskList) {
                    taskList.remove(this);
                }
                task.run();
            }
        };
    }

    public void removeAll() {
        synchronized (taskList) {
            for (Runnable runnable : taskList) {
                handler.removeCallbacks(runnable);
            }
            taskList.clear();
        }
    }

}
