package com.github.st1hy.sabre.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.collect.RestrictiveClassToInstanceMap;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.imagecache.storage.ImageCacheStorage;

class RestrictiveMapToImageCacheStorageAdapter implements ImageCacheStorage {
    private final RestrictiveClassToInstanceMap map;

    public RestrictiveMapToImageCacheStorageAdapter(RestrictiveClassToInstanceMap map) {
        this.map = map;
    }

    @Nullable
    @Override
    public ImageCache get() {
        return map.getInstance(ImageCache.class);
    }

    @Override
    public void set(@NonNull ImageCache cache) {
        map.putInstance(ImageCache.class, cache);
    }
}
