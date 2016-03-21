package com.github.st1hy.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.github.st1hy.core.State;

public abstract class AbstractScreen implements TransformableScreen {
    private final Matrix4 initialTransformation = new Matrix4();
    private final Matrix4 outputTransformation = new Matrix4();

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

    protected abstract void setTransformation(Matrix4 matrix4);
}
