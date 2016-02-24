package com.github.st1hy.imagecache.decoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface BitmapDecoder<Source> {

    @Nullable
    Bitmap decode(@NonNull Source source, @Nullable Rect outPadding, @Nullable BitmapFactory.Options options);
}
