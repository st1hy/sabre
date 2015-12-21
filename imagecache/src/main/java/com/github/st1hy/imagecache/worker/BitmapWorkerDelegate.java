package com.github.st1hy.imagecache.worker;


import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.st1hy.imagecache.BuildConfig;
import com.github.st1hy.imagecache.ImageCache;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

/**
 * Common bitmap worker behavior for every worker
 */
public class BitmapWorkerDelegate<T> implements Callable<T> {
    private static final String TAG = "BitmapWorkerDelegate";
    private final Object mPauseWorkLock;
    private final Uri uri;
    private final String cacheIndex;
    private final WeakReference<ImageReceiver<T>> imageViewReference;
    private volatile boolean isCancelled = false;
    private final BitmapWorkerTask.Callback<T> callback;
    private final ImageCache mImageCache;
    private boolean cacheOnDisk = true;
    private final BitmapWorkerTask parent;
    private Bitmap bitmap;

    public BitmapWorkerDelegate(@NonNull BitmapWorkerTask parent, @NonNull Uri uri, @NonNull ImageReceiver<T> imageView, @NonNull BitmapWorkerTask.Callback<T> callback) {
        this.parent = parent;
        this.uri = uri;
        cacheIndex = callback.getCacheIndex(uri);
        imageViewReference = new WeakReference<>(imageView);
        this.callback = callback;
        mImageCache = callback.getImageCache();
        mPauseWorkLock = callback.getSharedWaitingLock();
    }

    public String getCacheIndex() {
        return cacheIndex;
    }

    public void setCacheOnDisk(boolean cacheOnDisk) {
        this.cacheOnDisk = cacheOnDisk;
    }

    protected void onCancelled() {
        synchronized (mPauseWorkLock) {
            mPauseWorkLock.notifyAll();
        }
    }

    public void cancelTask() {
        isCancelled = true;
        this.bitmap = null;
    }

    @Override
    public T call() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "runnable - starting work");
        }
        Bitmap bitmap = null;
        T image = null;

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
        if (mImageCache != null) {
            mImageCache.addBitmapToCache(cacheIndex, bitmap, cacheOnDisk);
        }
        if (bitmap != null) {
            // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
            image = callback.createImage(bitmap);
            //drawable = new AsyncDrawable(mResources, bitmap, this);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "finished work " + image + " " + bitmap);
        }
        this.bitmap = bitmap;
        return image;
    }

    public void setImage(T image) {
        if (isCancelled() || callback.isExitingTaskEarly()) {
            image = null;
        }

        final ImageReceiver<T> imageView = getAttachedImageView();
        if (image != null && imageView != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "setting bitmap");
            }
            callback.setFinalImageAndReleasePrevious(imageView, image, bitmap);
        }
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Returns the ImageView associated with this task as long as the ImageView's task still
     * points to this task as well. Returns null otherwise.
     */
    private ImageReceiver<T> getAttachedImageView() {
        final ImageReceiver<T> imageView = imageViewReference.get();
        BitmapWorkerTask bitmapWorkerTask = callback.getBitmapWorkerTask(imageView);
        if (parent == bitmapWorkerTask) {
            return imageView;
        }
        return null;
    }
}
