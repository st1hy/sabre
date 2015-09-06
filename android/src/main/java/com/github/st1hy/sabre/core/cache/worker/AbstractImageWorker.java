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

package com.github.st1hy.sabre.core.cache.worker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.github.st1hy.sabre.BuildConfig;
import com.github.st1hy.sabre.core.cache.ImageCache;
import com.github.st1hy.sabre.core.cache.ImageResizer;

import java.util.WeakHashMap;
import java.util.concurrent.Executor;

/**
 * Taken from DisplayingBitmaps example.
 */
public abstract class AbstractImageWorker<T> implements ImageWorker<T>, BitmapWorkerTask.Callback<T> {
    private static final String TAG = "ImageWorker";
    protected static final int FADE_IN_TIME = 250;

    private final ImageCache mImageCache;
    protected final Context context;
    protected final Resources mResources;

    protected Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private TaskOption taskOption = TaskOption.RESULT_ON_MAIN_THREAD;

    private volatile boolean mExitTasksEarly = false;
    private volatile boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    protected WeakHashMap<ImageReceiver, BitmapWorkerTask> taskMap = new WeakHashMap<>();

    public AbstractImageWorker(Context context, ImageCache imageCache) {
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
    public void loadImage(Uri uri, ImageReceiver<T> imageView) {
        if (uri == null) {
            return;
        }
        Bitmap value = mImageCache.getBitmapFromMemCache(getCacheIndex(uri));
        if (value != null) {
            // Bitmap found in memory cache
            T image = createImage(value);
            imageView.setImage(image);
        } else if (cancelPotentialWork(uri, imageView)) {
            final BitmapWorkerTask task = taskOption.newTask(uri, imageView, this);
            taskMap.put(imageView, task);
            imageView.setImage(createImage(mLoadingBitmap));
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
    public void cancelWork(ImageReceiver<T> imageReceiver) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageReceiver);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancelTask(true);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "cancelWork - cancelled work for " + bitmapWorkerTask.getCacheIndex());
            }
        }
    }

    @Override
    public boolean cancelPotentialWork(Uri uri, ImageReceiver<T> imageReceiver) {
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
    public BitmapWorkerTask getBitmapWorkerTask(ImageReceiver<T> imageReceiver) {
        if (imageReceiver != null) {
            return taskMap.get(imageReceiver);
        }
        return null;
    }

    static String getCacheIndex(Uri uri) {
        return uri.getPath();
    }

    @Override
    public void setFinalImage(ImageReceiver<T> imageView, T image) {
        if (mFadeInBitmap) {
            // Set background to loading bitmap
            imageView.setBackground(createImage(mLoadingBitmap));
            imageView.setImage(createImageFadingIn(image));
        } else {
            imageView.setImage(image);
        }
    }

    public abstract T createImageFadingIn(T image);

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
