package com.github.st1hy.imagecache.worker;


import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.imagecache.BuildConfig;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.imagecache.reuse.RefHandle;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import timber.log.Timber;

/**
 * Common bitmap worker behavior for every worker
 */
public class BitmapWorkerDelegate<T> implements Callable<RefHandle<Bitmap>> {
    private final Object mPauseWorkLock;
    private final Uri uri;
    private final String cacheIndex;
    private final WeakReference<ImageReceiver<T>> imageViewReference;
    private volatile boolean isCancelled = false;
    private final BitmapWorkerTask.Callback<T> callback;
    private final ImageCache mImageCache;
    private boolean cacheOnDisk = true;
    private final BitmapWorkerTask parent;

    public BitmapWorkerDelegate(@NonNull BitmapWorkerTask parent, @NonNull Uri uri, @NonNull ImageReceiver<T> imageView, @NonNull BitmapWorkerTask.Callback<T> callback) {
        this.parent = parent;
        this.uri = uri;
        cacheIndex = callback.getCacheIndex(uri);
        imageViewReference = new WeakReference<>(imageView);
        this.callback = callback;
        mImageCache = callback.getImageCache();
        mPauseWorkLock = callback.getSharedWaitingLock();
    }

    @NonNull
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
    }

    @Override
    public RefHandle<Bitmap> call() {
        if (BuildConfig.DEBUG) {
            Timber.d("runnable - starting work");
        }
        RefHandle<Bitmap> bitmapHandle = null;

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
        // to this task and our "exit early" flag is not set then try and fetch the bitmapHandle from
        // the cache
        if (mImageCache != null && isTaskFresh()) {
            bitmapHandle = mImageCache.getBitmapFromDiskCache(cacheIndex);
        }

        // If the bitmapHandle was not found in the cache and this task has not been cancelled by
        // another thread and the ImageView that was originally bound to this task is still
        // bound back to this task and our "exit early" flag is not set, then call the main
        // process method (as implemented by a subclass)
        if (bitmapHandle == null && isTaskFresh()) {
            bitmapHandle = callback.readBitmap(uri);
        }

        // If the bitmapHandle was processed and the image cache is available, then add the processed
        // bitmapHandle to the cache for future use. Note we don't check if the task was cancelled
        // here, if it was, and the thread is still running, we may as well add the processed
        // bitmapHandle to our cache as it might be used again in the future
        if (mImageCache != null && bitmapHandle != null) {
            //We clone reference so that cache can take ownership over it.
            mImageCache.addBitmapToCache(cacheIndex, bitmapHandle.clone(), cacheOnDisk);
        }
        if (BuildConfig.DEBUG) {
            Timber.d("finished work " + bitmapHandle);
        }
        return bitmapHandle;
    }

    public void onBitmapRead(@Nullable RefHandle<Bitmap> image) {
        if (isCancelled() || callback.isExitingTaskEarly()) {
            image = null;
        }
        ImageReceiver<T> imageView = getAttachedImageView();
        if (imageView != null && isTaskFresh()) {
            if (BuildConfig.DEBUG) {
                Timber.d("setting bitmap");
            }
            callback.onBitmapRead(imageView, image);
        }
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * @return true if task still should be performed, false if it has been canceled or otherwise invalidated.
     */
    private boolean isTaskFresh() {
        return !isCancelled() && getAttachedImageView() != null && !callback.isExitingTaskEarly();
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
