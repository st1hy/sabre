package com.github.st1hy.sabre.core.cache.worker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import com.github.st1hy.sabre.core.cache.ImageCache;

public class DrawableImageWorker extends AbstractImageWorker<Drawable> {

    public DrawableImageWorker(Context context, ImageCache imageCache) {
        super(context, imageCache);
    }

    @Override
    public Drawable createImage(Bitmap bitmap) {
        return new BitmapDrawable(mResources, bitmap);
    }

    @Override
    public Drawable createImageFadingIn(Drawable image) {
        // Transition drawable with a transparent drawable and the final drawable
        TransitionDrawable td = new TransitionDrawable(new Drawable[]{new ColorDrawable(Color.TRANSPARENT), image});
        td.startTransition(FADE_IN_TIME);
        return td;
    }
}
