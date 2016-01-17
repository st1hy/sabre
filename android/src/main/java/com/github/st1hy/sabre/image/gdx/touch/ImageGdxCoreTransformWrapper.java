package com.github.st1hy.sabre.image.gdx.touch;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix3;
import com.github.st1hy.core.ImageGdxCore;
import com.github.st1hy.sabre.image.gdx.touch.transform.Matrix3ChangedListener;

public class ImageGdxCoreTransformWrapper implements Matrix3ChangedListener {
    private final ImageGdxCore core;

    public ImageGdxCoreTransformWrapper(@NonNull ImageGdxCore core) {
        this.core = core;
    }

    public void reset() {
        core.getTransformation().idt();
    }

    @Override
    public void onMatrix3Changed(@NonNull Matrix3 matrix3) {
        core.getTransformation().set(matrix3);
        Gdx.graphics.requestRendering();
    }
}
