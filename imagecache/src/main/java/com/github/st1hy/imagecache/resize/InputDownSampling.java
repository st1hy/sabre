package com.github.st1hy.imagecache.resize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

/**
 * Samples down input to keep at least on dimension of output image lower just above required.
 *  *
 * Internally calculates the largest inSampleSize value that is a power of 2 and keeps both
 * height and width larger than the requested height and width.
 *
 * Additionally it can downsample more aggressively for images of more unusual aspect ratios.
 * Internally it reduces further inSampleSize until total number of pixels in output image is not larger
 * than required times some factor. Look at {@link InputDownSampling#setAggressiveDownsamplingFactor(int)}
 *
 */
public class InputDownSampling implements ResizingStrategy {
    private boolean isAggressiveDownsampling = true;
    private int aggressiveDownsamplingFactor = 4;

    @Override
    public boolean isResizingRequired() {
        return true;
    }

    @Override
    public boolean isInSampleSizeUsed() {
        return true;
    }

    @Override
    public int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            if (isAggressiveDownsampling) {
                long totalPixels = width * height / inSampleSize / inSampleSize;

                final long totalReqPixelsCap = reqWidth * reqHeight * aggressiveDownsamplingFactor;

                while (totalPixels > totalReqPixelsCap) {
                    inSampleSize *= 2;
                    totalPixels /= 2;
                }
            }
        }
        return inSampleSize;
    }

    @NonNull
    @Override
    public Bitmap resizeBitmap(@NonNull Bitmap bitmap, int reqWidth, int reqHeight) {
        return bitmap;
    }

    public void setAggressiveDownsampling(boolean aggressiveDownsampling) {
        this.isAggressiveDownsampling = aggressiveDownsampling;
    }

    public void setAggressiveDownsamplingFactor(int aggressiveDownsamplingFactor) {
        this.aggressiveDownsamplingFactor = aggressiveDownsamplingFactor;
    }
}
