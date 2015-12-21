package com.github.st1hy.imagecache.worker;

import android.net.Uri;
import android.support.annotation.NonNull;

public interface LoaderFactory {

    @NonNull
    <T> BitmapWorkerTask newTask(@NonNull Uri uri, @NonNull ImageReceiver<T> receiver, @NonNull BitmapWorkerTask.Callback<T> callback);
}
