package com.github.st1hy.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public abstract class AbstractScreen implements TransformableScreen {
    private static final Matrix4 IDT = new Matrix4();
    private final Matrix4 initialTransformation = new Matrix4();
    private final Matrix4 outputTransformation = new Matrix4();
    private final Vector3 translation = new Vector3();
    private final Quaternion rotation = new Quaternion();

    @Override
    public void create() {
        initialTransformation.idt();
        outputTransformation.idt();
    }

    @Override
    public void onMatrix3Changed(State state, Matrix3 matrix3) {
        if (state == State.STARTED) {
            initialTransformation.set(outputTransformation);
        }
        outputTransformation.set(matrix3).mul(initialTransformation);
        setTransformation(outputTransformation);
        Gdx.graphics.requestRendering();
    }

    @Override
    public void onMatrix3Reset() {
        outputTransformation.idt();
        setTransformation(outputTransformation);
    }

    private void setTransformation(Matrix4 matrix4) {
//        matrix4.getTranslation(translation);
//        matrix4.getRotation(rotation);
//        float scaleZ = matrix4.getScaleZ();
        setTransformation(matrix4, rotation, translation, 1f);
    }

    protected abstract void setTransformation(Matrix4 matrix4, Quaternion rotation, Vector3 translation, float scaleZ);

}
