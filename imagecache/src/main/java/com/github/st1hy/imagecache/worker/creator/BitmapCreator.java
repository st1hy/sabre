package com.github.st1hy.imagecache.worker.creator;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BitmapCreator implements ImageCreator<Bitmap> {

    @Nullable
    @Override
    public Bitmap createImage(@Nullable Bitmap bitmap) {
        return bitmap;
    }

    @NonNull
    @Override
    public Bitmap createImageFadingIn(@NonNull Bitmap image, int fadeInTime) {
        return image;
    }
}
