package com.github.st1hy.imagecache.worker;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.github.st1hy.imagecache.worker.name.CacheEntryNameFactory;

/**
 * This interface wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public interface ImageWorker<T> extends CacheEntryNameFactory {
    /**
     * Loads image to imageView. If image exists in in-memory cache it will load image immediately.
     * Otherwise it will start asynchronous task that will load it.
     */
    void loadImage(@NonNull Uri uri, @NonNull ImageReceiver<T> imageView);

    void onDestroy();

    void setExitTasksEarly(boolean exitTasksEarly);

    /**
     * Cancels any pending work attached to the provided ImageReceiver.
     */
    void cancelWork(@NonNull ImageReceiver<T> imageReceiver);

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image receiver.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    boolean cancelPotentialWork(@NonNull Uri uri, @NonNull ImageReceiver<T> imageReceiver);

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p/>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    void setPauseWork(boolean pauseWork);

    /**
     * Starts async task that cleans cache. Returns immediately.
     */
    void clearCache();

    /**
     * Starts async task that flushes cache. Returns immediately.
     */
    void flushCache();

    /**
     * Starts async task that closes cache. Returns immediately.
     */
    void closeCache();
}
