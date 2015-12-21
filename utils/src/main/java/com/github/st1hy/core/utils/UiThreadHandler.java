package com.github.st1hy.core.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handler for running tasks on Ui Thread. This handler supports removing tasks on demand.
 * I.e. if ui related task is run every time activity starts, cancelling task when stopping may be
 * correct thing to do, protecting us from unnecessary work.
 */
public class UiThreadHandler {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Runnable> taskList = new CopyOnWriteArrayList<>();

    public void post(@NonNull Runnable runnable) {
        handler.post(appendList(runnable));
    }

    public void postDelayed(@NonNull Runnable runnable, long delayMillis) {
        handler.postDelayed(appendList(runnable), delayMillis);
    }

    private Runnable appendList(@NonNull Runnable runnable) {
        Runnable innerTask = createInternalTask(runnable);
        taskList.add(innerTask);
        return innerTask;
    }

    private Runnable createInternalTask(@NonNull Runnable task) {
        return new RemoveOnRunDecorator(taskList, task);
    }

    public void removeAll() {
        for (Runnable runnable : taskList) {
            handler.removeCallbacks(runnable);
        }
        taskList.clear();
    }

    private static class RemoveOnRunDecorator implements Runnable {
        private final List<Runnable> parent;
        private final Runnable child;

        private RemoveOnRunDecorator(@NonNull List<Runnable> parent, @NonNull Runnable child) {
            this.parent = parent;
            this.child = child;
        }

        @Override
        public void run() {
            parent.remove(this);
            child.run();
        }
    }

}
