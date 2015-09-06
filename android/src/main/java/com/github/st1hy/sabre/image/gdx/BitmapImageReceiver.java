package com.github.st1hy.sabre.image.gdx;

import android.graphics.Bitmap;

import com.github.st1hy.sabre.image.AsyncImageReceiver;

public class BitmapImageReceiver implements AsyncImageReceiver<Bitmap> {
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
