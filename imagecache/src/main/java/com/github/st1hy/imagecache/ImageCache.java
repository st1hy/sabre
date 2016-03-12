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

package com.github.st1hy.imagecache;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;

import com.github.st1hy.core.utils.Utils;
import com.github.st1hy.imagecache.decoder.FileDescriptorBitmapFactory;
import com.github.st1hy.imagecache.resize.KeepOriginal;
import com.github.st1hy.imagecache.reuse.RefHandle;
import com.github.st1hy.imagecache.reuse.ReusableBitmapPool;
import com.github.st1hy.imagecache.storage.ImageCacheStorage;
import com.github.st1hy.imagecache.worker.ImageWorkerImp;
import com.github.st1hy.retainer.Retainer;
import com.google.common.hash.Hashing;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import timber.log.Timber;

/**
 * Taken from DisplayingBitmaps example.
 * <p/>
 * *****************************************************************************
 * This class handles disk and memory caching of bitmaps in conjunction with the
 * {@link ImageWorkerImp} class and its subclasses. Use
 * {@link ImageCache#getInstance(ImageCacheStorage, ImageCacheParams)} to get an instance of this
 * class
 */
public class ImageCache {
    // Default memory cache size in kilobytes
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB

    // Default disk cache size in bytes
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

    // Compression settings when writing images to disk cache
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final int DISK_CACHE_INDEX = 0;

    // Constant to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

    private DiskLruCache mDiskLruCache;
    private final LruCache<String, RefHandle<Bitmap>> mMemoryCache;
    private final ImageCacheParams mCacheParams;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;

    private BitmapProvider<FileDescriptor> cachedBitmapProvider;
    private final ReusableBitmapPool reusableBitmapPool = new ReusableBitmapPool();

    /**
     * Create a new ImageCache object using the specified parameters. This should not be
     * called directly by other classes, instead use
     * {@link ImageCache#getInstance(ImageCacheStorage, ImageCacheParams)} to fetch an ImageCache
     * instance.
     *
     * @param cacheParams The cache parameters to use to initialize the cache
     */
    private ImageCache(ImageCacheParams cacheParams) {
        mCacheParams = cacheParams;

        if (mCacheParams.memoryCacheEnabled) {
            if (BuildConfig.DEBUG) {
                Timber.d("Memory cache created (size = %d )", mCacheParams.memCacheSize);
            }

            mMemoryCache = new LruCache<String, RefHandle<Bitmap>>(mCacheParams.memCacheSize) {

                /**
                 * Notify the removed entry that is no longer being cached
                 */
                @Override
                protected void entryRemoved(boolean evicted, String key, RefHandle<Bitmap> oldValue, RefHandle<Bitmap> newValue) {
                    oldValue.close();
                }

                /**
                 * Measure item size in kilobytes rather than units which is more practical
                 * for a bitmap cache
                 */
                @Override
                protected int sizeOf(String key, RefHandle<Bitmap> value) {
                    final int bitmapSize = getBitmapSize(value.get()) / 1024;
                    return bitmapSize == 0 ? 1 : bitmapSize;
                }
            };
        } else {
            mMemoryCache = null;
        }

        // By default the disk cache is not initialized here as it should be initialized
        // on a separate thread due to disk access.
        if (cacheParams.initDiskCacheOnCreate) {
            // Set up disk cache
            initDiskCache();
        }
    }

    /**
     * Return an {@link ImageCache} instance. A {@link Retainer} is used to retain the
     * ImageCache object across configuration changes such as a change in device orientation.
     *
     * @param storage The retainer instance to use when dealing with storing cache instance.
     * @param cacheParams     The cache parameters to use if the ImageCache needs instantiation.
     * @return An existing retained ImageCache object or a new one if one did not exist
     */
    public static ImageCache getInstance(@NonNull ImageCacheStorage storage, @NonNull ImageCacheParams cacheParams) {
        // See if we already have an ImageCache stored in RetainFragment
        ImageCache imageCache = storage.get();

        // No existing ImageCache, create one and store it in RetainFragment
        if (imageCache == null) {
            imageCache = new ImageCache(cacheParams.clone());
            storage.set(imageCache);
        }

        return imageCache;
    }


    /**
     * Initializes the disk cache.  Note that this includes disk access so this should not be
     * executed on the main/UI thread. By default an ImageCache does not initialize the disk
     * cache when it is created, instead you should call initDiskCache() to initialize it on a
     * background thread.
     */
    public void initDiskCache() {
        // Set up disk cache
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = mCacheParams.diskCacheDir;
                if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        diskCacheDir.mkdirs();
                    }
                    if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
                        try {
                            mDiskLruCache = DiskLruCache.open(
                                    diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
                            if (BuildConfig.DEBUG) {
                                Timber.d("Disk cache initialized");
                            }
                        } catch (final IOException e) {
                            mCacheParams.diskCacheDir = null;
                            Timber.e(e, "initDiskCache failed");
                        }
                    }
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }


    /**
     * Adds a bitmap to both memory and disk cache.
     *
     * @param data  Unique identifier for the bitmap to store
     * @param bitmapHandle The bitmap reference to store, image cache will take ownership of provided handle
     */
    public void addBitmapToCache(@NonNull String data, @NonNull RefHandle<Bitmap> bitmapHandle) {
        addBitmapToCache(data, bitmapHandle, true);
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     *
     * @param data  Unique identifier for the bitmap to store
     * @param bitmapHandle The bitmap reference to store, image cache will take ownership of provided handle
     * @param cacheOnDisk true if disk cache will be used, false - only memory cache will be used
     */
    public void addBitmapToCache(@NonNull String data, @NonNull RefHandle<Bitmap> bitmapHandle, boolean cacheOnDisk) {
        // Add to memory cache
        if (mMemoryCache != null) {
            mMemoryCache.put(data, bitmapHandle);
        }
        if (cacheOnDisk) {
            addToDiskCache(data, bitmapHandle);
        }
    }

    private void addToDiskCache(@NonNull String data, @NonNull RefHandle<Bitmap> bitmapHandle) {
        Bitmap bitmap = bitmapHandle.getOrNull();
        if (bitmap == null) {
            Timber.w("Cannot add to disk cache, bitmap reference has been closed!");
            return;
        }
        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            try {
                                out = editor.newOutputStream(DISK_CACHE_INDEX);
                                bitmap.compress(mCacheParams.compressFormat, mCacheParams.compressQuality, out);
                                editor.commit();
                            } finally {
                                if (out != null) {
                                    out.close();
                                }
                            }
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (Exception e) {
                    Timber.e(e, "Adding bitmap to disk cache failed");
                }
            }
        }
    }

    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap drawable if found in cache, null otherwise, returned bitmap reference is owned by the receiver.
     */
    @Nullable
    public RefHandle<Bitmap> getBitmapFromMemCache(String data) {
        RefHandle<Bitmap> memValue = null;

        if (mMemoryCache != null) {
            memValue = mMemoryCache.get(data);
            if (memValue != null) memValue = memValue.clone();
        }

        if (BuildConfig.DEBUG && memValue != null) {
            Timber.d("Memory cache hit %s bitmap %s", data, memValue.get().toString());
        }

        return memValue;
    }

    /**
     * Get from disk cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise, returned handle is owned by the receiver
     */
    @Nullable
    public RefHandle<Bitmap> getBitmapFromDiskCache(String data) {
        if (!mCacheParams.diskCacheEnabled) return null;
        final String key = hashKeyForDisk(data);
        RefHandle<Bitmap> bitmapHandle = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Timber.d("Disk cache hit");
                        }
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                            // Decode bitmapHandle, but we don't want to sample so give
                            // MAX_VALUE as the target dimensions
                            bitmapHandle = getCachedBitmapProvider().getImage(fd);
                        }
                    }
                } catch (final IOException e) {
                    Timber.e(e, "getBitmapFromDiskCache failed");
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        Timber.e(e, "getBitmapFromDiskCache failed closing stream");
                    }
                }
            }
            return bitmapHandle;
        }
    }

    @NonNull
    private BitmapProvider<FileDescriptor> getCachedBitmapProvider() {
        if (cachedBitmapProvider == null) {
            BitmapProvider.Builder<FileDescriptor> builder = new BitmapProvider.Builder<>(new FileDescriptorBitmapFactory());
            builder.setResizingStrategy(new KeepOriginal());
            builder.setReusableBitmapPool(reusableBitmapPool);
            cachedBitmapProvider = builder.build();
        }
        return cachedBitmapProvider;
    }

    public void clearMemory() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
            if (BuildConfig.DEBUG) {
                Timber.d("Memory cache cleared");
            }
        }
    }

    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache() {
        clearMemory();

        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                    if (BuildConfig.DEBUG) {
                        Timber.d("Disk cache cleared");
                    }
                } catch (IOException e) {
                    Timber.e(e, "clearCache failed");
                }
                mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                    if (BuildConfig.DEBUG) {
                        Timber.d("Disk cache flushed");
                    }
                } catch (IOException e) {
                    Timber.e(e, "Disk cache flush failed");
                }
            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                        if (BuildConfig.DEBUG) {
                            Timber.d("Disk cache closed");
                        }
                    }
                } catch (IOException e) {
                    Timber.e(e, "closing disk cache failed");
                }
            }
        }
    }

    /**
     * A holder class that contains cache parameters.
     */
    public static class ImageCacheParams {
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public File diskCacheDir;
        public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

        ImageCacheParams(ImageCacheParams params) {
            this.memCacheSize = params.memCacheSize;
            this.diskCacheSize = params.diskCacheSize;
            this.diskCacheDir = params.diskCacheDir;
            this.compressFormat = params.compressFormat;
            this.compressQuality = params.compressQuality;
            this.memoryCacheEnabled = params.memoryCacheEnabled;
            this.diskCacheEnabled = params.diskCacheEnabled;
            this.initDiskCacheOnCreate = params.initDiskCacheOnCreate;
        }

        /**
         * Create a set of image cache parameters that can be provided to
         * {@link ImageCache#getInstance(ImageCacheStorage, ImageCacheParams)}
         *
         * @param context                A context to use.
         * @param diskCacheDirectoryName A unique subdirectory name that will be appended to the
         *                               application cache directory. Usually "cache" or "images"
         *                               is sufficient.
         */
        public ImageCacheParams(Context context, String diskCacheDirectoryName) {
            diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
        }

        /**
         * Sets the memory cache size based on a percentage of the max available VM memory.
         * Eg. setting percent to 0.2 would set the memory cache to one fifth of the available
         * memory. Throws {@link IllegalArgumentException} if percent is < 0.01 or > .8.
         * memCacheSize is stored in kilobytes instead of bytes as this will eventually be passed
         * to construct a LruCache which takes an int in its constructor.
         * <p/>
         * This value should be chosen carefully based on a number of factors
         * Refer to the corresponding Android Training class for more discussion:
         * http://developer.android.com/training/displaying-bitmaps/
         *
         * @param percent Percent of available app memory to use to size memory cache
         */
        public void setMemCacheSizePercent(float percent) {
            if (percent < 0.01f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                        + "between 0.01 and 0.8 (inclusive)");
            }
            memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
        }

        public ImageCacheParams clone() {
            return new ImageCacheParams(this);
        }
    }


    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context    The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        File cachePathFile = isMediaMountedOrBuildIn() ? context.getExternalCacheDir() : context.getCacheDir();
        if (cachePathFile == null) cachePathFile = context.getCacheDir();
        final String cachePath = cachePathFile.getPath();
        return new File(cachePath + File.separator + uniqueName);
    }

    private static boolean isMediaMountedOrBuildIn() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable();
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        return Hashing.md5().hashString(key, Charset.defaultCharset()).toString();
    }

    /**
     * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat)
     * onward this returns the allocated memory size of the bitmap which can be larger than the
     * actual bitmap data byte count (in the case it was re-used).
     *
     * @param bitmap
     * @return size in bytes
     */
    @TargetApi(VERSION_CODES.KITKAT)
    public static int getBitmapSize(Bitmap bitmap) {
        // From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
        // larger than bitmap byte count.
        if (Utils.hasKitKat()) {
            return bitmap.getAllocationByteCount();
        }
        return bitmap.getByteCount();
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    public static long getUsableSpace(File path) {
        return path.getUsableSpace();
    }

    @NonNull
    public ReusableBitmapPool getReusableBitmapPool() {
        return reusableBitmapPool;
    }
}
