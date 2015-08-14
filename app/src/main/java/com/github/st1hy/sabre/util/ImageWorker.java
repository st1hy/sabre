/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.st1hy.sabre.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.github.st1hy.sabre.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/**
 * Taken from DisplayingBitmaps example.
 * <p/>
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public class ImageWorker {
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 250;

    private final ImageCache mImageCache;
    protected final Context context;
    protected final Resources mResources;

    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();


    public ImageWorker(Context context, ImageCache imageCache) {
        this.context = context;
        mImageCache = imageCache;
        mResources = context.getResources();
    }

    /**
     * Loads image to imageView. If image exists in in-memory cache it will load image immediately.
     * Otherwise it will start asynchronous task that will load it.
     *
     * @param uri
     * @param imageView
     */
    public void loadImage(Uri uri, ImageReceiver imageView) {
        if (uri == null) {
            return;
        }
        BitmapDrawable value = mImageCache.getBitmapFromMemCache(getCacheIndex(uri));
        if (value != null) {
            // Bitmap found in memory cache
            imageView.setImageDrawable(value);
        } else if (cancelPotentialWork(uri, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(uri, imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.executeOnExecutor(getExecutor());
        }
    }

    public interface ImageReceiver {
        void setImageDrawable(Drawable drawable);

        void setBackground(Drawable drawable);

        Drawable getDrawable();

        Drawable getBackground();
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    /**
     * Subclasses should override this to define any processing or work that must happen to produce
     * the final bitmap. This will be executed in a background thread and be long running. For
     * example, you could resize a large bitmap here, or pull down an image from the network.
     *
     * @param uri The data to identify which image to process, as provided by
     *            {@link ImageWorker#loadImage(Uri, ImageReceiver)}
     * @return The processed bitmap
     */
    protected Bitmap processBitmap(Uri uri) {
        return ImageResizer.decodeUri(uri, Integer.MAX_VALUE, Integer.MAX_VALUE, mImageCache, context.getContentResolver());
    }

    /**
     * @return The {@link ImageCache} object currently being used by this ImageWorker.
     */
    protected ImageCache getImageCache() {
        return mImageCache;
    }

    /**
     * Cancels any pending work attached to the provided ImageReceiver.
     *
     * @param imageReceiver
     */
    public static void cancelWork(ImageReceiver imageReceiver) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageReceiver);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "cancelWork - cancelled work for " + bitmapWorkerTask.cacheIndex);
            }
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public static boolean cancelPotentialWork(Uri uri, ImageReceiver imageReceiver) {
        //BEGIN_INCLUDE(cancel_potential_work)
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageReceiver);

        if (bitmapWorkerTask != null) {
            final String cacheIndex = bitmapWorkerTask.cacheIndex;
            if (cacheIndex == null || !cacheIndex.equals(getCacheIndex(uri))) {
                bitmapWorkerTask.cancel(true);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "cancelPotentialWork - cancelled work for " + cacheIndex);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
        //END_INCLUDE(cancel_potential_work)
    }

    /**
     * @param imageReceiver Any imageReceiver
     * @return Retrieve the currently active work task (if any) associated with this imageReceiver.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageReceiver imageReceiver) {
        if (imageReceiver != null) {
            final Drawable drawable = imageReceiver.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<Void, Void, BitmapDrawable> {
        private final Uri uri;
        private final String cacheIndex;
        private final WeakReference<ImageReceiver> imageViewReference;

        public BitmapWorkerTask(Uri uri, ImageReceiver imageView) {
            this.uri = uri;
            cacheIndex = getCacheIndex(uri);
            imageViewReference = new WeakReference<>(imageView);
        }

        /**
         * Background processing.
         */
        @Override
        protected BitmapDrawable doInBackground(Void... params) {
            //BEGIN_INCLUDE(load_bitmap_in_background)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - starting work");
            }
            Bitmap bitmap = null;
            BitmapDrawable drawable = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(cacheIndex);
            }

            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = processBitmap(uri);
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null) {
                // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
                drawable = new BitmapDrawable(mResources, bitmap);
                //drawable = new AsyncDrawable(mResources, bitmap, this);
            }
            if (mImageCache != null) {
                mImageCache.addBitmapToCache(cacheIndex, drawable);
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - finished work " + drawable + " " + bitmap);
            }

            return drawable;
            //END_INCLUDE(load_bitmap_in_background)
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            //BEGIN_INCLUDE(complete_background_work)
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                drawable = null;
            }

            final ImageReceiver imageView = getAttachedImageView();
            if (drawable != null && imageView != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onPostExecute - setting bitmap");
                }
                setImageDrawable(imageView, drawable);
            }
            //END_INCLUDE(complete_background_work)
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
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask) {
                return imageView;
            }
            return null;
        }
    }

    private static String getCacheIndex(Uri uri) {
        return uri.getPath();
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * Called when the processing is complete and the final drawable should be
     * set on the ImageView.
     *
     * @param imageView
     * @param drawable
     */
    private void setImageDrawable(ImageReceiver imageView, Drawable drawable) {
        if (mFadeInBitmap) {
            // Transition drawable with a transparent drawable and the final drawable
            final TransitionDrawable td = new TransitionDrawable(new Drawable[]{new ColorDrawable(Color.TRANSPARENT), drawable});
            // Set background to loading bitmap
            imageView.setBackground(new BitmapDrawable(mResources, mLoadingBitmap));
            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p/>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    /**
     * Starts async task that cleans cache. Returns immediately.
     */
    public void clearCache() {
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mImageCache.clearCache();
            }
        });
    }

    /**
     * Starts async task that flushes cache. Returns immediately.
     */
    public void flushCache() {
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mImageCache.flush();
            }
        });
    }

    /**
     * Starts async task that closes cache. Returns immediately.
     */
    public void closeCache() {
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mImageCache.close();
            }
        });
    }

    private Executor getExecutor() {
        return AsyncTask.THREAD_POOL_EXECUTOR;
    }
}
