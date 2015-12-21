package com.github.st1hy.imagecache.worker;

import android.net.Uri;
import android.support.annotation.NonNull;

public enum SimpleLoaderFactory implements LoaderFactory {

    RESULT_ON_MAIN_THREAD {
        @Override
        @NonNull
        public <T> BitmapWorkerTask newTask(@NonNull Uri uri, @NonNull ImageReceiver<T> receiver, @NonNull BitmapWorkerTask.Callback<T> callback) {
            return new BitmapWorkerTaskAsyncTask<>(uri, receiver, callback);
        }
    }, RUNNABLE {
        @Override
        @NonNull
        public <T> BitmapWorkerTask newTask(@NonNull Uri uri, @NonNull ImageReceiver<T> receiver, @NonNull BitmapWorkerTask.Callback<T> callback) {
            return new BitmapWorkerRunnable<>(uri, receiver, callback);
        }
    },
    WITHOUT_DISK_CACHE {
        @Override
        @NonNull
        public <T> BitmapWorkerTask newTask(@NonNull Uri uri, @NonNull ImageReceiver<T> receiver, @NonNull BitmapWorkerTask.Callback<T> callback) {
            return new BitmapWorkerRunnable<>(uri, receiver, callback).cacheOnDisk(false);
        }
    };


}
