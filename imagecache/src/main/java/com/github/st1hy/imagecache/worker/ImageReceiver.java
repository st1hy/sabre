package com.github.st1hy.imagecache.worker;

import android.support.annotation.Nullable;

public interface ImageReceiver<T> {
    void onImageLoadingFailed();

    void setImage(@Nullable T image);

    void setBackground(@Nullable T background);
}
