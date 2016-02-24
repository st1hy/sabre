package com.github.st1hy.imagecache.worker.creator;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public interface ImageCreator<T> {

    @NonNull
    T createImage(@NonNull Bitmap bitmap);

    @NonNull
    T createImageFadingIn(@NonNull T image, int fadeInTime);
}
