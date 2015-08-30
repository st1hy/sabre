package com.github.st1hy.sabre.gdx.image;

import android.net.Uri;

public interface ImageViewer {

    void setLoadingCallback(ImageLoadingCallback loadingCallback);

    void setImageURI(Uri uri);

    void setVisibility(int visibility);

    interface ImageLoadingCallback {
        void onImageLoadingStarted();

        void onImageLoadingFinished();
    }
}
