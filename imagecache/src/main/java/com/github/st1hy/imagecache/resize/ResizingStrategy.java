package com.github.st1hy.imagecache.resize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

/**
 * Specifies how bitmaps should be resized if their size is different than required.
 */
public interface ResizingStrategy {

    /**
     *
     * @return true if resizing is required, false if you want to read entire image directly and skip resizing.
     */
    boolean isResizingRequired();

    /**
     *
     * @return true if inBitmap can be used
     */
    boolean isReusingBitmaps();

    /**
     * When resizing you may want to use inSampleSize to reduce amount of allocated memory for image.
     *
     * The loaded resolution of image equals initial resolution / inSampleSize.
     *
     * @return true if inSampleSize is in use or not.
     */
    boolean isInSampleSizeUsed();

    /**
     *
     * The loaded resolution of image equals initial resolution / inSampleSize.
     *
     * @param options filled options containing image size
     * @param reqWidth required width of output image
     * @param reqHeight required height of output image
     * @return resulting in sample size.
     */
    int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight);

    @NonNull
    Bitmap resizeBitmap(@NonNull Bitmap bitmap, int reqWidth, int reqHeight);
}
