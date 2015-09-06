package com.github.st1hy.sabre.core.cache.worker;

import android.net.Uri;

public interface LoaderFactory {

    <T> BitmapWorkerTask newTask(Uri uri, ImageReceiver<T> receiver, BitmapWorkerTask.Callback<T> callback);
}
