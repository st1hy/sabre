package com.github.st1hy.imagecache.worker;

import android.content.Context;
import android.graphics.Bitmap;

import com.github.st1hy.imagecache.ImageCache;

public class BitmapImageWorker extends AbstractImageWorker<Bitmap> {

    public BitmapImageWorker(Context context, ImageCache imageCache) {
        super(context, imageCache);
    }

    @Override
    public Bitmap createImageFadingIn(Bitmap image) {
        return image;
    }

    @Override
    public Bitmap createImage(Bitmap bitmap) {
        return bitmap;
    }
}
