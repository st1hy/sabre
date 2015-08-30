package com.github.st1hy.sabre.cache.worker;

import android.net.Uri;

public enum TaskOption {
    RESULT_ON_MAIN_THREAD {
        @Override
        <T> BitmapWorkerTask newTask(Uri uri, ImageReceiver<T> receiver, BitmapWorkerTask.Callback<T> callback) {
            return new BitmapWorkerTaskAsyncTask(uri, receiver, callback);
        }
    }, RUNNABLE {
        @Override
        <T> BitmapWorkerTask newTask(Uri uri, ImageReceiver<T> receiver, BitmapWorkerTask.Callback<T> callback) {
            return new BitmapWorkerRunnable(uri, receiver, callback);
        }
    };

    abstract <T> BitmapWorkerTask newTask(Uri uri, ImageReceiver<T> receiver, BitmapWorkerTask.Callback<T> callback);
}
