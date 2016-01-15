package com.github.st1hy.imagecache.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.imagecache.ImageCache;

public interface ImageCacheStorage {

    @Nullable
    ImageCache get();

    void set(@NonNull ImageCache cache);
}
