package com.github.st1hy.sabre.core.cache.worker;

import android.net.Uri;

public class SimpleCacheEntryNameFactory implements CacheEntryNameFactory {

    @Override
    public String getCacheIndex(Uri uri) {
        return uri.getPath();
    }
}
