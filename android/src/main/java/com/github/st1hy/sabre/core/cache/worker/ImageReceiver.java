package com.github.st1hy.sabre.core.cache.worker;

public interface ImageReceiver<T> {
    void setImage(T image);

    void setBackground(T background);

    T getImage();

    T getBackground();


    interface Callback {
        void onImageLoaded();

        void redrawNeeded();
    }
}
