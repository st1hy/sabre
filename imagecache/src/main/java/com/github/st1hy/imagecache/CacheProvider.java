package com.github.st1hy.imagecache;

import android.support.annotation.NonNull;

public interface CacheProvider {

    @NonNull
    CacheHandler getCacheHandler();
}
