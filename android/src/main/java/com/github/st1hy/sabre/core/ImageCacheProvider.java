package com.github.st1hy.sabre.core;

import android.support.annotation.NonNull;

import com.github.st1hy.imagecache.ImageCacheHandler;

public interface ImageCacheProvider {

    @NonNull
    ImageCacheHandler getImageCacheHandler();
}
