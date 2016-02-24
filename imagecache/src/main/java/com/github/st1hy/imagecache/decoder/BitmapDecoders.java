package com.github.st1hy.imagecache.decoder;

import java.io.FileDescriptor;

public final class BitmapDecoders {

    private BitmapDecoders() {
    }

    public static BitmapDecoder<FileDescriptor> newFileDescriptorBitmapFactory() {
        return new FileDescriptorBitmapFactory();
    }

    public static BitmapDecoder<ResourceBitmapSource> newImageResourceBitmapFactory() {
        return new ImageResourceBitmapFactory();
    }

    public static BitmapDecoder<String> newFileBitmapFactory() {
        return new FileBitmapFactory();
    }

    public static BitmapDecoder<UriBitmapSource> newUriBitmapFactory() {
        return new UriBitmapFactory();
    }
}
