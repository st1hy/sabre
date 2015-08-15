package com.github.st1hy.sabre.image.worker;

import android.graphics.drawable.Drawable;

public interface ImageReceiver {
    void setImageDrawable(Drawable drawable);

    void setBackground(Drawable drawable);

    Drawable getDrawable();

    Drawable getBackground();
}
