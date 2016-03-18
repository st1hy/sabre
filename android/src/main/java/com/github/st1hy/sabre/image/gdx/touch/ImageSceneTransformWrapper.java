package com.github.st1hy.sabre.image.gdx.touch;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Matrix3;
import com.github.st1hy.core.ImageScene;
import com.github.st1hy.core.Matrix3ChangedListener;

public class ImageSceneTransformWrapper implements Matrix3ChangedListener {
    private ImageScene scene;

    public void setScene(@Nullable ImageScene scene) {
        this.scene = scene;
    }

    public void reset() {
        if (scene != null) scene.resetMatrix();
    }

    @Override
    public void onMatrix3Changed(@NonNull Matrix3 matrix3) {
        if (scene != null) scene.onMatrix3Changed(matrix3);
    }
}
