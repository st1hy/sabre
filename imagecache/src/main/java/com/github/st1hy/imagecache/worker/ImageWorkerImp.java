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

package com.github.st1hy.imagecache.worker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.core.utils.Utils;
import com.github.st1hy.imagecache.BitmapProvider;
import com.github.st1hy.imagecache.BuildConfig;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.imagecache.decoder.UriBitmapFactory;
import com.github.st1hy.imagecache.decoder.UriBitmapSource;
import com.github.st1hy.imagecache.resize.ResizingStrategy;
import com.github.st1hy.imagecache.worker.creator.ImageCreator;
import com.github.st1hy.imagecache.worker.name.CacheEntryNameFactory;
import com.github.st1hy.imagecache.worker.name.SimpleCacheEntryNameFactory;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;

import timber.log.Timber;

public class ImageWorkerImp<T> implements ImageWorker<T> {
    private final Context context;
    private final ImageCreator<T> imageCreator;
    private ImageCache imageCache;
    @Nullable
    private Bitmap loadingBitmap;
    private int fadeInTime;
    private LoaderFactory loaderFactory;
    private CacheEntryNameFactory cacheEntryNameFactory;
    private BitmapProvider<UriBitmapSource> bitmapProvider;
    private TaskCallback<T> taskCallback;
    private Executor executor;

    protected final Map<ImageReceiver, BitmapWorkerTask> taskMap = Collections.synchronizedMap(new WeakHashMap<ImageReceiver, BitmapWorkerTask>());

    private ImageWorkerImp(@NonNull Context context, @NonNull ImageCreator<T> imageCreator) {
        this.context = context;
        this.imageCreator = imageCreator;
    }

    private static class TaskCallback<T> implements BitmapWorkerTask.Callback<T> {
        private final ImageWorkerImp<T> parent;
        private final Object mPauseWorkLock = new Object();
        private volatile boolean mExitTasksEarly = false;
        private volatile boolean mPauseWork = false;

        public TaskCallback(@NonNull ImageWorkerImp<T> parent) {
            this.parent = parent;
        }

        @Override
        public Bitmap readBitmap(@NonNull Uri uri) {
            UriBitmapSource source = UriBitmapSource.of(parent.context.getContentResolver(), uri);
            return parent.bitmapProvider.getImage(source);
        }

        @Override
        public ImageCache getImageCache() {
            return parent.imageCache;
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

        @Override
        public void onBitmapRead(@NonNull ImageReceiver<T> imageView, @Nullable Bitmap bitmap) {
            if (bitmap != null) {
                T image;
                if (parent.fadeInTime > 0) {
                    image = parent.imageCreator.createImageFadingIn(bitmap, parent.fadeInTime);
                } else {
                    image = parent.imageCreator.createImage(bitmap);
                }
                parent.setImage(imageView, image);
            } else {
                imageView.onImageLoadingFailed();
            }
        }

        @Override
        public BitmapWorkerTask getBitmapWorkerTask(@NonNull ImageReceiver imageReceiver) {
            return parent.taskMap.get(imageReceiver);
        }

        @NonNull
        @Override
        public String getCacheIndex(@NonNull Uri uri) {
            return parent.cacheEntryNameFactory.getCacheIndex(uri);
        }

        public void setPauseWork(boolean pauseWork) {
            synchronized (mPauseWorkLock) {
                mPauseWork = pauseWork;
                if (!mPauseWork) {
                    mPauseWorkLock.notifyAll();
                }
            }
        }
    }

    @Override
    public void loadImage(@NonNull Uri uri, @NonNull ImageReceiver<T> imageView) {
        T loadingImage = loadingBitmap != null ? imageCreator.createImage(loadingBitmap) : null;
        imageView.setBackground(loadingImage);
        Bitmap value = imageCache.getBitmapFromMemCache(getCacheIndex(uri));
        if (value != null) {
            // Bitmap found in memory cache
            T image = imageCreator.createImage(value);
            setImage(imageView, image);
        } else if (cancelPotentialWork(uri, imageView)) {
            final BitmapWorkerTask task = loaderFactory.newTask(uri, imageView, taskCallback);
            taskMap.put(imageView, task);
            setImage(imageView, loadingImage);
            task.executeOnExecutor(executor);
        }
    }

    @Override
    public void setExitTasksEarly(boolean exitTasksEarly) {
        taskCallback.mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    @Override
    public void cancelWork(@NonNull ImageReceiver<T> imageReceiver) {
        BitmapWorkerTask bitmapWorkerTask = taskCallback.getBitmapWorkerTask(imageReceiver);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancelTask(true);
            if (BuildConfig.DEBUG) {
                Timber.d("cancelWork - cancelled work for %d", bitmapWorkerTask.getCacheIndex());
            }
        }
    }

    @Override
    public boolean cancelPotentialWork(@NonNull Uri uri, @NonNull ImageReceiver<T> imageReceiver) {
        BitmapWorkerTask bitmapWorkerTask = taskCallback.getBitmapWorkerTask(imageReceiver);
        if (bitmapWorkerTask != null) {
            final String cacheIndex = bitmapWorkerTask.getCacheIndex();
            if (cacheIndex == null || !cacheIndex.equals(getCacheIndex(uri))) {
                bitmapWorkerTask.cancelTask(true);
                if (BuildConfig.DEBUG) {
                    Timber.d("cancelPotentialWork - cancelled work for %d", cacheIndex);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }


    @Override
    @NonNull
    public String getCacheIndex(@NonNull Uri uri) {
        return cacheEntryNameFactory.getCacheIndex(uri);
    }

    private void setImage(@NonNull ImageReceiver<T> imageView, @Nullable T image) {
        imageView.setImage(image);
    }

    @Override
    public void setPauseWork(boolean pauseWork) {
        taskCallback.setPauseWork(pauseWork);
    }

    @Override
    public void clearCache() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                imageCache.clearCache();
            }
        });
    }

    @Override
    public void flushCache() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                imageCache.flush();
            }
        });
    }

    @Override
    public void closeCache() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                imageCache.close();
            }
        });
    }

    public static class Builder<T> {
        private final Context context;
        private final ImageCreator<T> imageCreator;
        private ImageCache imageCache;
        private Bitmap loadingBitmap;
        private int fadeInTime = 250;
        private LoaderFactory loaderFactory = SimpleLoaderFactory.RESULT_ON_MAIN_THREAD;
        private CacheEntryNameFactory cacheEntryNameFactory;
        private Executor executor;

        private int reqWidth = Integer.MAX_VALUE, reqHeight = Integer.MAX_VALUE;
        private ResizingStrategy resizingStrategy;

        public Builder(@NonNull Context context, @NonNull ImageCreator<T> imageCreator) {
            this.context = context;
            this.imageCreator = imageCreator;
        }

        public Builder setImageCache(@Nullable ImageCache imageCache) {
            this.imageCache = imageCache;
            return this;
        }

        public Builder setRequestedSize(int reqWidth, int reqHeight) {
            this.reqWidth = reqWidth;
            this.reqHeight = reqHeight;
            return this;
        }

        public Builder setCacheEntryNameFactory(@NonNull CacheEntryNameFactory cacheEntryNameFactory) {
            this.cacheEntryNameFactory = cacheEntryNameFactory;
            return this;
        }

        public Builder setResizingStrategy(@NonNull ResizingStrategy resizingStrategy) {
            this.resizingStrategy = resizingStrategy;
            return this;
        }

        /**
         * Set placeholder bitmap that shows when the the background thread is running.
         *
         */
        public Builder setLoadingImage(@Nullable Bitmap bitmap) {
            loadingBitmap = bitmap;
            return this;
        }

        /**
         * Set placeholder bitmap that shows when the the background thread is running.
         *
         * @param resId image resource id
         */
        public Builder setLoadingImage(int resId) {
            loadingBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
            return this;
        }

        /**
         * Set to 0 to disable fade-in, else the image will fade-in once it has been loaded by the background thread.
         */
        public Builder setImageFadeIn(int fadeInTime) {
            this.fadeInTime = fadeInTime;
            return this;
        }

        public Builder setLoaderFactory(@NonNull LoaderFactory loaderFactory) {
            this.loaderFactory = loaderFactory;
            return this;
        }

        public Builder setExecutor(@NonNull Executor executor) {
            this.executor = executor;
            return this;
        }

        public ImageWorkerImp<T> build() {
            ImageWorkerImp<T> worker = new ImageWorkerImp<>(context, imageCreator);
            worker.imageCache = imageCache;
            worker.loadingBitmap = loadingBitmap;
            worker.fadeInTime = fadeInTime;
            worker.loaderFactory = loaderFactory;
            if (cacheEntryNameFactory == null) cacheEntryNameFactory = new SimpleCacheEntryNameFactory();
            worker.cacheEntryNameFactory = cacheEntryNameFactory;
            worker.bitmapProvider = buildBitmapProvider();
            worker.taskCallback = new TaskCallback<>(worker);
            if (executor == null) executor = Utils.CACHED_EXECUTOR_POOL;
            worker.executor = executor;
            return worker;
        }

        private BitmapProvider<UriBitmapSource> buildBitmapProvider() {
            BitmapProvider.Builder<UriBitmapSource> builder = new BitmapProvider.Builder<>(new UriBitmapFactory());
            builder.setRequiredSize(reqWidth, reqHeight);
            builder.setResizingStrategy(resizingStrategy);
            return builder.build();
        }
    }
}
