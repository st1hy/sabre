package com.github.st1hy.imagecache.decoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.FileDescriptor;

public class FileDescriptorBitmapFactory implements BitmapDecoder<FileDescriptor> {
    @Override
    @Nullable
    public Bitmap decode(@NonNull FileDescriptor fileDescriptor, @Nullable Rect outPadding, @Nullable BitmapFactory.Options options) {
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, outPadding, options);
    }
}
