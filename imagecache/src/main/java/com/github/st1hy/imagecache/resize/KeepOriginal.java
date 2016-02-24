package com.github.st1hy.imagecache.resize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

/**
 * Doesn't resize the image. Open image in full size.
 */
public class KeepOriginal implements ResizingStrategy {

    @Override
    public boolean isResizingRequired() {
        return false;
    }

    @Override
    public boolean isInSampleSizeUsed() {
        return false;
    }

    @Override
    public int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        return 1;
    }

    @NonNull
    @Override
    public Bitmap resizeBitmap(@NonNull Bitmap bitmap, int reqWidth, int reqHeight) {
        return bitmap;
    }
}
