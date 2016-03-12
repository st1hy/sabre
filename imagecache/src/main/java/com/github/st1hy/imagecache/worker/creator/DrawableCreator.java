package com.github.st1hy.imagecache.worker.creator;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;

public class DrawableCreator implements ImageCreator<Drawable> {
    private final Resources resources;

    public DrawableCreator(@NonNull Resources resources) {
        this.resources = resources;
    }

    @NonNull
    @Override
    public Drawable createImage(@NonNull Bitmap bitmap) {
        return new BitmapDrawable(resources, bitmap);
    }

    @NonNull
    @Override
    public Drawable createImageFadingIn(@NonNull Bitmap image, int fadeInTime) {
        // Transition drawable with a transparent drawable and the final drawable
        TransitionDrawable td = new TransitionDrawable(new Drawable[]{new ColorDrawable(Color.TRANSPARENT), createImage(image)});
        td.startTransition(fadeInTime);
        return td;
    }


}
