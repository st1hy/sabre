package com.github.st1hy.imagecache.decoder;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;

import java.io.IOException;
import java.io.InputStream;

public class ImageResourceBitmapFactory implements BitmapDecoder<ResourceBitmapSource> {
    @Nullable
    @Override
    public Bitmap decode(@NonNull ResourceBitmapSource source, @Nullable Rect outPadding, @Nullable BitmapFactory.Options opts) {
        Bitmap bm = null;
        InputStream is = null;
        Resources res = source.resources;
        int id = source.imageResId;

        try {
            final TypedValue value = new TypedValue();
            is = res.openRawResource(id, value);

            bm = BitmapFactory.decodeResourceStream(res, value, is, outPadding, opts);
        } catch (Exception e) {
        /*  do nothing.
            If the exception happened on open, bm will be null.
            If it happened on close, bm is still valid.
        */
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        if (bm == null && opts != null && opts.inBitmap != null) {
            throw new IllegalArgumentException("Problem decoding into existing bitmap");
        }

        return bm;
    }
}
