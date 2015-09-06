package com.github.st1hy.sabre.core.cache.worker;

import android.net.Uri;

public interface CacheEntryNameFactory {

    String getCacheIndex(Uri uri);
}
