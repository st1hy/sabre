package com.github.st1hy.imagecache.decoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class UriBitmapFactory implements BitmapDecoder<UriBitmapSource> {

    @Nullable
    @Override
    public Bitmap decode(@NonNull UriBitmapSource source, @Nullable Rect outPadding, @Nullable BitmapFactory.Options options) {
        try {
            InputStream input = null;
            try {
                input = source.resolver.openInputStream(source.uri);
                return BitmapFactory.decodeStream(input, outPadding, options);
            } finally {
                if (input != null) input.close();
            }
        } catch (IOException e) {
            Timber.e(e, "Uri content could not be resolved!");
            return null;
        }
    }
}
