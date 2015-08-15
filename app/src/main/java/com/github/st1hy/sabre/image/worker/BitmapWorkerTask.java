package com.github.st1hy.sabre.image.worker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.github.st1hy.sabre.image.ImageCache;

import java.util.concurrent.Executor;

interface BitmapWorkerTask {
    String getCacheIndex();
    void cancelTask(boolean interruptIFRunning);
    void executeOnExecutor(Executor executor);

    interface Callback {
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
         * Called when the processing is complete and the final drawable should be
         * set on the ImageView.
         *
         * @param imageView
         * @param drawable
         */
        void setImageDrawable(ImageReceiver imageView, Drawable drawable);

        /**
         * @return The {@link ImageCache} object currently being used..
         */
        ImageCache getImageCache();

        Resources getResources();

        Object getSharedWaitingLock();

        boolean isWaitingRequired();

        boolean isExitingTaskEarly();
    }
}
