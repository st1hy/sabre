package com.github.st1hy.imagecache.reuse;

import android.support.annotation.NonNull;

public class RefHandle<T> {
    T ref;
    final RefCounter<T> counter;
    boolean closed = false;

    RefHandle(@NonNull T ref, @NonNull RefCounter<T> counter) {
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

    public void close() {
        synchronized (counter) {
            closed = true;
            counter.decrement();
            ref = null;
        }
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


    private void checkState() {
        if (closed) throw new IllegalStateException("Cannot access reference after close");
    }
}
