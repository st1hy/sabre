package com.github.st1hy.imagecache.worker;

public interface ImageReceiver<T> {
    void setImage(T image);

    void setBackground(T background);
}
