package com.github.st1hy.sabre.image.surface;

import android.net.Uri;

import com.github.st1hy.sabre.core.cache.ImageCache;

public interface ImageViewer {
    void addImageCache(ImageCache cache);

    void setLoadingCallback(ImageLoadingCallback loadingCallback);

    void setImageURI(Uri uri);

    void setVisibility(int visibility);

    void onPause();

    void onResume();

    void onDestroy();

    interface ImageLoadingCallback {
        void onImageLoadingStarted();

        void onImageLoadingFinished();
    }
}
