package com.github.st1hy.imagecache;

import android.support.annotation.NonNull;

public interface ImageCacheProvider {

    @NonNull
    ImageCacheHandler getImageCacheHandler();
}
