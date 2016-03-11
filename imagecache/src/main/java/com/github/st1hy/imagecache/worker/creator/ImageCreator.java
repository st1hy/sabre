package com.github.st1hy.imagecache.worker.creator;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface ImageCreator<T> {

    @Nullable
    T createImage(@Nullable Bitmap bitmap);

    @NonNull
    T createImageFadingIn(@NonNull Bitmap image, int fadeInTime);
}
