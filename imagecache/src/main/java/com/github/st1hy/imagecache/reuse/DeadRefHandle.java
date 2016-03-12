package com.github.st1hy.imagecache.reuse;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Reference handle without counter.
 * @param <T>
 */
class DeadRefHandle<T> extends RefHandle<T> {

    DeadRefHandle(@NonNull T ref) {
         super(ref, null);
    }

    @NonNull
    @Override
    public T get() {
        checkState();
        return ref;
    }

    @Nullable
    @Override
    public T getOrNull() {
        return ref;
    }

    @Override
    public void close() {
        closed = true;
        ref = null;
    }

    @Override
    public RefHandle<T> clone() {
        checkState();
        return new DeadRefHandle<>(ref);
    }
}
