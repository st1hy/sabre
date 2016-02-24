package com.github.st1hy.imagecache.worker.name;

import android.net.Uri;
import android.support.annotation.NonNull;

public class SimpleCacheEntryNameFactory implements CacheEntryNameFactory {

    @Override
    @NonNull
    public String getCacheIndex(@NonNull Uri uri) {
        return uri.getPath();
    }
}
