package com.github.st1hy.imagecache.decoder;

import android.content.res.Resources;
import android.support.annotation.NonNull;

public class ResourceBitmapSource {
    public final Resources resources;
    public final int imageResId;

    private ResourceBitmapSource(@NonNull Resources resources, int imageResId) {
        this.resources = resources;
        this.imageResId = imageResId;
    }

    public static ResourceBitmapSource of(@NonNull Resources resources, int imageResId) {
        return new ResourceBitmapSource(resources, imageResId);
    }
}
