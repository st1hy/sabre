package com.github.st1hy.sabre.core.cache.worker;

import android.graphics.Bitmap;
import android.net.Uri;

import com.github.st1hy.sabre.core.cache.ImageCache;

import java.util.concurrent.Executor;

interface BitmapWorkerTask {
    String getCacheIndex();

    void cancelTask(boolean interruptIFRunning);

    void executeOnExecutor(Executor executor);

    interface Callback<T> {
        /**
         * Subclasses should override this to define any processing or work that must happen to produce
         * the final bitmap. This will be executed in a background thread and be long running. For
         * example, you could resize a large bitmap here, or pull down an image from the network.
         *
         * @param uri The data to identify which image to process, as provided by
         *            {@link ImageWorker#loadImage(Uri, ImageReceiver)}
         * @return The processed bitmap
         */
        Bitmap processBitmap(Uri uri);

        /**
         * Called when the processing is complete and the final image should be
         * set on the ImageView.
         *
         * @param imageView
         * @param image
         */
        void setFinalImage(ImageReceiver<T> imageView, T image);

        /**
         * @return The {@link ImageCache} object currently being used..
         */
        ImageCache getImageCache();

        T createImage(Bitmap bitmap);

        Object getSharedWaitingLock();

        boolean isWaitingRequired();

        boolean isExitingTaskEarly();

        BitmapWorkerTask getBitmapWorkerTask(ImageReceiver<T> imageReceiver);
    }
}
