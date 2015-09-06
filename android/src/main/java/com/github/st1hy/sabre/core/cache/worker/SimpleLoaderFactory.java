package com.github.st1hy.sabre.core.cache.worker;

import android.net.Uri;

public enum SimpleLoaderFactory implements LoaderFactory {

    RESULT_ON_MAIN_THREAD {
        @Override
        public <T> BitmapWorkerTask newTask(Uri uri, ImageReceiver<T> receiver, BitmapWorkerTask.Callback<T> callback) {
            return new BitmapWorkerTaskAsyncTask<>(uri, receiver, callback);
        }
    }, RUNNABLE {
        @Override
        public <T> BitmapWorkerTask newTask(Uri uri, ImageReceiver<T> receiver, BitmapWorkerTask.Callback<T> callback) {
            return new BitmapWorkerRunnable<>(uri, receiver, callback);
        }
    },
    WITHOUT_DISK_CACHE {
        @Override
        public <T> BitmapWorkerTask newTask(Uri uri, ImageReceiver<T> receiver, BitmapWorkerTask.Callback<T> callback) {
            return new BitmapWorkerRunnable<>(uri, receiver, callback).cacheOnDisk(false);
        }
    };


}
