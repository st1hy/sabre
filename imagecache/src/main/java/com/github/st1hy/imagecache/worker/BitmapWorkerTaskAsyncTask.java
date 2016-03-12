package com.github.st1hy.imagecache.worker;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.github.st1hy.imagecache.reuse.RefHandle;

import java.util.concurrent.Executor;

/**
 * The actual AsyncTask that will asynchronously process the image.
 */
class BitmapWorkerTaskAsyncTask<T> extends AsyncTask<Void, Void, RefHandle<Bitmap>> implements BitmapWorkerTask {
    private final BitmapWorkerDelegate workerDelegate;

    public BitmapWorkerTaskAsyncTask(@NonNull Uri uri, @NonNull ImageReceiver<T> imageView, @NonNull BitmapWorkerTask.Callback<T> callback) {
        workerDelegate = new BitmapWorkerDelegate<>(this, uri, imageView, callback);
    }

    @Override
    protected RefHandle<Bitmap> doInBackground(Void... params) {
        return workerDelegate.call();
    }

    /**
     * Once the image is processed, associates it to the imageView
     */
    @Override
    protected void onPostExecute(RefHandle<Bitmap> bitmap) {
        workerDelegate.onBitmapRead(bitmap);
    }

    @Override
    protected void onCancelled(RefHandle<Bitmap> bitmap) {
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
