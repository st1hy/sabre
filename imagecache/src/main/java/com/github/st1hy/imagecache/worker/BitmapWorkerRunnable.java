package com.github.st1hy.imagecache.worker;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

class BitmapWorkerRunnable<T> implements BitmapWorkerTask, Runnable {
    private final BitmapWorkerDelegate workerDelegate;

    public BitmapWorkerRunnable(@NonNull Uri uri, @NonNull ImageReceiver<T> imageView, @NonNull BitmapWorkerTask.Callback<T> callback) {
        workerDelegate = new BitmapWorkerDelegate<>(this, uri, imageView, callback);
    }

    public BitmapWorkerRunnable cacheOnDisk(boolean cacheOnDisk) {
        workerDelegate.setCacheOnDisk(cacheOnDisk);
        return this;
    }

    @Override
    public String getCacheIndex() {
        return workerDelegate.getCacheIndex();
    }

    @Override
    public void executeOnExecutor(Executor executor) {
        executor.execute(this);
    }

    @Override
    public void cancelTask(boolean interruptIFRunning) {
        workerDelegate.cancelTask();
        workerDelegate.onCancelled();
    }

    @Override
    public void run() {
        Bitmap bitmap = workerDelegate.call();
        workerDelegate.onBitmapRead(bitmap);
    }
}
