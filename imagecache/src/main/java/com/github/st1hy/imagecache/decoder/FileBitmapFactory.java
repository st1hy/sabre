package com.github.st1hy.imagecache.decoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class FileBitmapFactory implements BitmapDecoder<String> {

    @Nullable
    @Override
    public Bitmap decode(@NonNull String filename, @Nullable Rect outPadding, @Nullable BitmapFactory.Options options) {
        Bitmap bm = null;
        InputStream stream = null;
        try {
            stream = new FileInputStream(filename);
            bm = BitmapFactory.decodeStream(stream, outPadding, options);
        } catch (Exception e) {
            /*  do nothing.
                If the exception happened on open, bm will be null.
            */
            Timber.e("BitmapFactory", "Unable to decode stream: " + e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // do nothing here
                }
            }
        }
        return bm;
    }
}
