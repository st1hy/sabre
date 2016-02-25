package com.github.st1hy.sabre.history;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.github.st1hy.imagecache.worker.ImageReceiver;

public class ImageReceiverView extends ImageView implements ImageReceiver<Drawable> {

    public ImageReceiverView(Context context) {
        super(context);
    }

    public ImageReceiverView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageReceiverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ImageReceiverView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setImage(@Nullable Drawable image) {
        setImageDrawable(image);
    }

}
