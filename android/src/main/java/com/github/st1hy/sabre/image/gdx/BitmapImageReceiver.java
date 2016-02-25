package com.github.st1hy.sabre.image.gdx;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.github.st1hy.sabre.image.AsyncImageReceiver;

public class BitmapImageReceiver implements AsyncImageReceiver<Bitmap> {
    private final Callback callback;
    private Bitmap image, background;

    public BitmapImageReceiver(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setImage(@Nullable Bitmap image) {
        this.image = image;
        if (image == null) {
            callback.onImageLoadingFailed();
        } else {
            callback.onImageLoaded();
            callback.redrawNeeded();
        }
    }

    @Override
    public void setBackground(@Nullable Bitmap background) {
        this.background = background;
    }

    @Override
    public Bitmap getImage() {
        return image;
    }

    @Override
    public Bitmap getBackground() {
        return background;
    }
}
