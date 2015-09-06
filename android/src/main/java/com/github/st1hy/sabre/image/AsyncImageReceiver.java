package com.github.st1hy.sabre.image;

import com.github.st1hy.sabre.core.cache.worker.ImageReceiver;

public interface AsyncImageReceiver<T> extends ImageReceiver<T> {

    T getImage();

    T getBackground();


    interface Callback {
        void onImageLoaded();

        void redrawNeeded();
    }
}
