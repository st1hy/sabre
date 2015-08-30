package com.github.st1hy.sabre.gdx.image;

import android.graphics.Bitmap;

import com.github.st1hy.sabre.cache.worker.ImageReceiver;

public class BitmapImageReceiver implements ImageReceiver<Bitmap> {
    private final Callback callback;
    private Bitmap image, background;

    public BitmapImageReceiver(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setImage(Bitmap image) {
        this.image = image;
        callback.onImageLoaded();
        callback.redrawNeeded();
    }

    @Override
    public void setBackground(Bitmap background) {
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
