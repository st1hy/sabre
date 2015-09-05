package com.github.st1hy.sabre.cache.worker;

import android.net.Uri;

import com.github.st1hy.sabre.image.bitmap.ImageReceiver;

public enum TaskOption {
    RESULT_ON_MAIN_THREAD{
        @Override
        BitmapWorkerTask newTask(Uri uri, ImageReceiver receiver, BitmapWorkerTask.Callback callback) {
            return new BitmapWorkerTaskAsyncTask(uri, receiver, callback);
        }
    }, RUNNABLE {
        @Override
        BitmapWorkerTask newTask(Uri uri, ImageReceiver receiver, BitmapWorkerTask.Callback callback) {
            return new BitmapWorkerRunnable(uri, receiver, callback);
        }
    };

    abstract BitmapWorkerTask newTask(Uri uri, ImageReceiver receiver, BitmapWorkerTask.Callback callback);
}
