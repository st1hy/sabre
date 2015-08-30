package com.github.st1hy.sabre.image.worker;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 *
 * This interface wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public interface ImageWorker {
    void setTaskOption(TaskOption taskOption);

    /**
     * Loads image to imageView. If image exists in in-memory cache it will load image immediately.
     * Otherwise it will start asynchronous task that will load it.
     *
     * @param uri
     * @param imageView
     */
    void loadImage(Uri uri, ImageReceiver imageView);

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    void setLoadingImage(Bitmap bitmap);

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    void setLoadingImage(int resId);

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    void setImageFadeIn(boolean fadeIn);

    void setExitTasksEarly(boolean exitTasksEarly);

    /**
     * Cancels any pending work attached to the provided ImageReceiver.
     *
     * @param imageReceiver
     */
    void cancelWork(ImageReceiver imageReceiver);

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image receiver.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    boolean cancelPotentialWork(Uri uri, ImageReceiver imageReceiver);

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
