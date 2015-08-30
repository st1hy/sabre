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

package com.github.st1hy.sabre.image.worker;

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
import com.github.st1hy.sabre.image.ImageCache;
import com.github.st1hy.sabre.image.ImageResizer;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/**
 * Taken from DisplayingBitmaps example.
 */
public class ImageWorkerImp implements ImageWorker, BitmapWorkerTask.Callback{
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 250;

    private final ImageCache mImageCache;
    protected final Context context;
    protected final Resources mResources;

    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private TaskOption taskOption = TaskOption.RESULT_ON_MAIN_THREAD;

    private volatile boolean mExitTasksEarly = false;
    private volatile boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    public ImageWorkerImp(Context context, ImageCache imageCache) {
        this.context = context;
        mImageCache = imageCache;
        mResources = context.getResources();
    }

    @Override
    public void setTaskOption(TaskOption taskOption) {
        if (taskOption == null) throw new NullPointerException();
        this.taskOption = taskOption;
    }

    @Override
    public void loadImage(Uri uri, ImageReceiver imageView) {
        if (uri == null) {
            return;
        }
        BitmapDrawable value = mImageCache.getBitmapFromMemCache(getCacheIndex(uri));
        if (value != null) {
            // Bitmap found in memory cache
            imageView.setImageDrawable(value);
        } else if (cancelPotentialWork(uri, imageView)) {
            final BitmapWorkerTask task = taskOption.newTask(uri, imageView, this);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.executeOnExecutor(getExecutor());
        }
    }

    @Override
    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    @Override
    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    @Override
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    @Override
    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    @Override
    public Bitmap processBitmap(Uri uri) {
        return ImageResizer.decodeUri(uri, Integer.MAX_VALUE, Integer.MAX_VALUE, mImageCache, context.getContentResolver());
    }

    @Override
    public ImageCache getImageCache() {
        return mImageCache;
    }

    @Override
    public void cancelWork(ImageReceiver imageReceiver) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageReceiver);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancelTask(true);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "cancelWork - cancelled work for " + bitmapWorkerTask.getCacheIndex());
            }
        }
    }

    @Override
    public boolean cancelPotentialWork(Uri uri, ImageReceiver imageReceiver) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageReceiver);

        if (bitmapWorkerTask != null) {
            final String cacheIndex = bitmapWorkerTask.getCacheIndex();
            if (cacheIndex == null || !cacheIndex.equals(getCacheIndex(uri))) {
                bitmapWorkerTask.cancelTask(true);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "cancelPotentialWork - cancelled work for " + cacheIndex);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageReceiver Any imageReceiver
     * @return Retrieve the currently active work task (if any) associated with this imageReceiver.
     * null if there is no such task.
     */
    static BitmapWorkerTask getBitmapWorkerTask(ImageReceiver imageReceiver) {
        if (imageReceiver != null) {
            final Drawable drawable = imageReceiver.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static String getCacheIndex(Uri uri) {
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

    @Override
    public void setImageDrawable(ImageReceiver imageView, Drawable drawable) {
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

    @Override
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    @Override
    public void clearCache() {
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mImageCache.clearCache();
            }
        });
    }

    @Override
    public void flushCache() {
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mImageCache.flush();
            }
        });
    }

    @Override
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

    @Override
    public Resources getResources() {
        return mResources;
    }

    @Override
    public Object getSharedWaitingLock() {
        return mPauseWorkLock;
    }

    @Override
    public boolean isWaitingRequired() {
        return mPauseWork;
    }

    @Override
    public boolean isExitingTaskEarly() {
        return mExitTasksEarly;
    }
}
