package com.github.st1hy.imagecache.worker.name;

import android.net.Uri;
import android.support.annotation.NonNull;

public interface CacheEntryNameFactory {

    @NonNull
    String getCacheIndex(@NonNull Uri uri);
}
