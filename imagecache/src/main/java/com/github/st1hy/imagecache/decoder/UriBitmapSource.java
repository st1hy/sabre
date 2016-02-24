package com.github.st1hy.imagecache.decoder;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;

public class UriBitmapSource {
    public final ContentResolver resolver;
    public final Uri uri;

    private UriBitmapSource(@NonNull ContentResolver resolver, @NonNull Uri uri) {
        this.resolver = resolver;
        this.uri = uri;
    }

    public static UriBitmapSource of(@NonNull ContentResolver resolver, @NonNull Uri uri) {
        return new UriBitmapSource(resolver, uri);
    }
}
