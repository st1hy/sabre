package com.github.st1hy.imagecache.worker;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.imagecache.reuse.RefHandle;
import com.github.st1hy.imagecache.worker.name.CacheEntryNameFactory;

import java.util.concurrent.Executor;

interface BitmapWorkerTask {
    String getCacheIndex();

    void cancelTask(boolean interruptIFRunning);

    void executeOnExecutor(Executor executor);

    interface Callback<T> extends CacheEntryNameFactory {
        /**
         * Subclasses should override this to define any processing or work that must happen to produce
         * the final bitmap. This will be executed in a background thread and be long running. For
         * example, you could resize a large bitmap here, or pull down an image from the network.
         *
         * @param uri The data to identify which image to process, as provided by
         *            {@link ImageWorker#loadImage(Uri, ImageReceiver)}
         * @return The processed bitmap, owned by the receiver
         */
        RefHandle<Bitmap> readBitmap(@NonNull Uri uri);

        /**
         * Called when the processing is complete and the final image has been read.
         *
         * @param bitmap bitmap as read from the source
         */
        void onBitmapRead(@NonNull ImageReceiver<T> imageView, @Nullable RefHandle<Bitmap> bitmap);

        /**
         * @return The {@link ImageCache} object currently being used..
         */
        ImageCache getImageCache();

        Object getSharedWaitingLock();

        boolean isWaitingRequired();

        boolean isExitingTaskEarly();

        BitmapWorkerTask getBitmapWorkerTask(ImageReceiver<T> imageReceiver);
    }
}
