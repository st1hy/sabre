package com.github.st1hy.imagecache.worker.creator;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public class BitmapCreator implements ImageCreator<Bitmap> {

    @NonNull
    @Override
    public Bitmap createImage(@NonNull Bitmap bitmap) {
        return bitmap;
    }

    @NonNull
    @Override
    public Bitmap createImageFadingIn(@NonNull Bitmap image, int fadeInTime) {
        return image;
    }
}
