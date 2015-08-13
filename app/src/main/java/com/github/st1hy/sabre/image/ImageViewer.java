package com.github.st1hy.sabre.image;

import android.content.Context;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.AttributeSet;

public class ImageViewer extends Viewer {

    public ImageViewer(Context context) {
        super(context);
    }
    public ImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ImageViewer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();

    }

    public void setImageURI(Uri uri) {

    }

    @Override
    public void drawContent(Canvas canvas) {

    }

    public interface ImageLoadingCallback {
        void onImageLoadingStarted();
        void onImageLoadingFinished();
    }
}
