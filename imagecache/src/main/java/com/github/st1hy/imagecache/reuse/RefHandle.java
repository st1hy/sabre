package com.github.st1hy.imagecache.reuse;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RefHandle<T> {
    protected T ref;
    private final RefCounter<T> counter;
    protected boolean closed = false;

    protected RefHandle(@NonNull T ref, RefCounter<T> counter) {
        this.ref = ref;
        this.counter = counter;
    }

    /**
     *
     * @return reference
     * @throws IllegalStateException if accessing after close
     */
    @NonNull
    public T get() {
        synchronized (counter) {
            checkState();
            return ref;
        }
    }

    @Nullable
    public T getOrNull() {
        synchronized (counter) {
            return ref;
        }
    }

    public void close() {
        synchronized (counter) {
            closed = true;
            counter.decrement();
            ref = null;
        }
    }

    public static <T> RefHandle<T> newHandle(@NonNull T ref) {
        return new DeadRefHandle<>(ref);
    }

    public static <T> RefHandle<T> newHandle(@NonNull T ref, @Nullable RefCounter.Callback<T> callback) {
        return new RefCounter<>(ref, callback).newHandle();
    }

    /**
     * @return clone of this handle
     * @throws IllegalStateException if accessing after close
     */
    public RefHandle<T> clone() {
        synchronized (counter) {
            checkState();
            return counter.newHandle();
        }
    }

    public boolean isClosed() {
        return closed;
    }

    protected void checkState() {
        if (closed) throw new IllegalStateException("Cannot access reference after close");
    }
}
