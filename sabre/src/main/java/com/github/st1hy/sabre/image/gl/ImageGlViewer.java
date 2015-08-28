package com.github.st1hy.sabre.image.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;

import com.github.st1hy.sabre.image.ImageCache;
import com.github.st1hy.sabre.image.ImageResizer;
import com.github.st1hy.sabre.image.ImageViewer;

public class ImageGlViewer extends GlViewer implements ImageViewer {

    public ImageGlViewer(Context context) {
        super(context);
    }

    public ImageGlViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addImageCache(ImageCache cache) {

    }

    @Override
    public void setLoadingCallback(ImageLoadingCallback loadingCallback) {

    }

    @Override
    public void setImageURI(final Uri uri) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = ImageResizer.decodeUri(uri, Integer.MAX_VALUE, Integer.MAX_VALUE, null, getContext().getContentResolver());
                renderer.setTexture(bitmap);
                requestRender();
            }
        });
    }
}
