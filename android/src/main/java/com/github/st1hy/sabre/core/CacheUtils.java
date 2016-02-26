package com.github.st1hy.sabre.core;

import com.github.st1hy.imagecache.ImageCacheHandler;
import com.github.st1hy.imagecache.storage.ImageCacheStorage;
import com.github.st1hy.sabre.Application;

public enum CacheUtils {
    ;

    public static ImageCacheHandler newImageCacheHandler(Application app) {
        ImageCacheStorage storageAdapter = new RestrictiveMapToImageCacheStorageAdapter(app.getCache());
        return ImageCacheHandler.getInstance(app, storageAdapter);
    }

}
