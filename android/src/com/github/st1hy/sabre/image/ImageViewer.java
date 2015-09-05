package com.github.st1hy.sabre.image;

import android.net.Uri;

import com.github.st1hy.sabre.cache.ImageCache;

public interface ImageViewer {
    void addImageCache(ImageCache cache);

    void setLoadingCallback(ImageLoadingCallback loadingCallback);

    void setImageURI(Uri uri);

    void setVisibility(int visibility);

    void onPause();

    void onResume();

    interface ImageLoadingCallback {
        void onImageLoadingStarted();

        void onImageLoadingFinished();
    }
}
