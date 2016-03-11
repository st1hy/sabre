package com.github.st1hy.imagecache.reuse;

public class RefCounter<T> {
    T ref;
    int value = 0;

    public RefHandle<T> newHandle() {
        synchronized (this) {
            RefHandle<T> handle = new RefHandle<>(ref, this);
            value++;
            return handle;
        }
    }

    public int getValue() {
        synchronized (this) {
            return value;
        }
    }

    void decrement() {
        synchronized (this) {
            value--;
        }
    }
}
