package com.github.st1hy.imagecache.reuse;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RefCounter<T> {
    private T ref;
    private int value = 0;
    private final Callback<T> callback;

    public RefCounter(@NonNull T ref) {
        this(ref, null);
    }

    public RefCounter(@NonNull T ref, @Nullable Callback<T> callback) {
        this.callback = callback;
        this.ref = ref;
    }

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
            if (value == 0) {
                if (callback != null) callback.onUnreachable(ref);
                ref = null;
            }
        }
    }

    public interface Callback<T> {
        /**
         * Called when reference stored in reference counter is no longer reachable, meaning that
         * all its handles has been closed.
         *
         * @param ref unreachable reference
         */
        void onUnreachable(@NonNull T ref);
    }
}
