package com.github.st1hy.imagecache.worker;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * The actual AsyncTask that will asynchronously process the image.
 */
class BitmapWorkerTaskAsyncTask<T> extends AsyncTask<Void, Void, T> implements BitmapWorkerTask {
    private final BitmapWorkerDelegate<T> workerDelegate;

    public BitmapWorkerTaskAsyncTask(@NonNull Uri uri, @NonNull ImageReceiver<T> imageView, @NonNull BitmapWorkerTask.Callback<T> callback) {
        workerDelegate = new BitmapWorkerDelegate<>(this, uri, imageView, callback);
    }

    @Override
    protected T doInBackground(Void... params) {
        return workerDelegate.call();
    }

    /**
     * Once the image is processed, associates it to the imageView
     */
    @Override
    protected void onPostExecute(T drawable) {
        workerDelegate.setImage(drawable);
    }

    @Override
    protected void onCancelled(T value) {
        super.onCancelled();
        workerDelegate.onCancelled();
    }

    @Override
    public String getCacheIndex() {
        return workerDelegate.getCacheIndex();
    }

    @Override
    public void executeOnExecutor(Executor executor) {
        super.executeOnExecutor(executor);
    }

    @Override
    public void cancelTask(boolean interruptIFRunning) {
        cancel(interruptIFRunning);
        workerDelegate.cancelTask();
    }
}
