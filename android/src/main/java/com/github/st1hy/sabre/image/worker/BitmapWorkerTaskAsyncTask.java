package com.github.st1hy.sabre.image.worker;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.github.st1hy.sabre.BuildConfig;
import com.github.st1hy.sabre.image.ImageCache;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/**
 * The actual AsyncTask that will asynchronously process the image.
 */
class BitmapWorkerTaskAsyncTask extends AsyncTask<Void, Void, BitmapDrawable> implements BitmapWorkerTask {
    private static final String TAG = "BitmapWorkerAsyncTask";
    private final Object mPauseWorkLock;

    private final Uri uri;
    private final String cacheIndex;
    private final WeakReference<ImageReceiver> imageViewReference;
    private final BitmapWorkerTask.Callback callback;
    private final ImageCache mImageCache;

    public BitmapWorkerTaskAsyncTask(Uri uri, ImageReceiver imageView, BitmapWorkerTask.Callback callback) {
        this.uri = uri;
        cacheIndex = ImageWorkerImp.getCacheIndex(uri);
        imageViewReference = new WeakReference<>(imageView);
        this.callback = callback;
        mImageCache = callback.getImageCache();
        mPauseWorkLock = callback.getSharedWaitingLock();
    }

    /**
     * Background processing.
     */
    @Override
    protected BitmapDrawable doInBackground(Void... params) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "starting work");
        }
        Bitmap bitmap = null;
        BitmapDrawable drawable = null;

        // Wait here if work is paused and the task is not cancelled
        synchronized (mPauseWorkLock) {
            while (callback.isWaitingRequired() && !isCancelled()) {
                try {
                    mPauseWorkLock.wait();
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
        }

        // If the image cache is available and this task has not been cancelled by another
        // thread and the ImageView that was originally bound to this task is still bound back
        // to this task and our "exit early" flag is not set then try and fetch the bitmap from
        // the cache
        if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                && !callback.isExitingTaskEarly()) {
            bitmap = mImageCache.getBitmapFromDiskCache(cacheIndex);
        }

        // If the bitmap was not found in the cache and this task has not been cancelled by
        // another thread and the ImageView that was originally bound to this task is still
        // bound back to this task and our "exit early" flag is not set, then call the main
        // process method (as implemented by a subclass)
        if (bitmap == null && !isCancelled() && getAttachedImageView() != null
                && !callback.isExitingTaskEarly()) {
            bitmap = callback.processBitmap(uri);
        }

        // If the bitmap was processed and the image cache is available, then add the processed
        // bitmap to the cache for future use. Note we don't check if the task was cancelled
        // here, if it was, and the thread is still running, we may as well add the processed
        // bitmap to our cache as it might be used again in the future
        if (bitmap != null) {
            // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
            drawable = new BitmapDrawable(callback.getResources(), bitmap);
            //drawable = new AsyncDrawable(mResources, bitmap, this);
        }
        if (mImageCache != null) {
            mImageCache.addBitmapToCache(cacheIndex, drawable);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "finished work " + drawable + " " + bitmap);
        }

        return drawable;
    }

    /**
     * Once the image is processed, associates it to the imageView
     */
    @Override
    protected void onPostExecute(BitmapDrawable drawable) {
        // if cancel was called on this task or the "exit early" flag is set then we're done
        if (isCancelled() || callback.isExitingTaskEarly()) {
            drawable = null;
        }

        final ImageReceiver imageView = getAttachedImageView();
        if (drawable != null && imageView != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "setting bitmap");
            }
            callback.setImageDrawable(imageView, drawable);
        }
    }

    @Override
    protected void onCancelled(BitmapDrawable value) {
        super.onCancelled(value);
        synchronized (mPauseWorkLock) {
            mPauseWorkLock.notifyAll();
        }
    }

    /**
     * Returns the ImageView associated with this task as long as the ImageView's task still
     * points to this task as well. Returns null otherwise.
     */
    private ImageReceiver getAttachedImageView() {
        final ImageReceiver imageView = imageViewReference.get();
        final BitmapWorkerTask bitmapWorkerTask = ImageWorkerImp.getBitmapWorkerTask(imageView);
        if (this == bitmapWorkerTask) {
            return imageView;
        }
        return null;
    }

    @Override
    public String getCacheIndex() {
        return cacheIndex;
    }

    @Override
    public void executeOnExecutor(Executor executor) {
        super.executeOnExecutor(executor);
    }

    @Override
    public void cancelTask(boolean interruptIFRunning) {
        cancel(interruptIFRunning);
    }
}
