package com.github.st1hy.imagecache.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.retainer.Retainer;

public class RetainerToImageStorageAdapter implements ImageCacheStorage {
    private static final String TAG = RetainerToImageStorageAdapter.class.getCanonicalName();
    private final Retainer retainer;

    public RetainerToImageStorageAdapter(@NonNull Retainer retainer) {
        this.retainer = retainer;
    }

    @Nullable
    @Override
    public ImageCache get() {
        return (ImageCache) retainer.get(TAG);
    }

    @Override
    public void set(@NonNull ImageCache cache) {
        retainer.put(TAG, cache);
    }
}
