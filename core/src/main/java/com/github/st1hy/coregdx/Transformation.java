package com.github.st1hy.coregdx;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class Transformation {
    private final Matrix4 initialTransformation = new Matrix4();
    private final Matrix4 transformation = new Matrix4();
    private final Matrix4 invTransformation = new Matrix4();
    private boolean dirty = false;

    public Transformation() {
    }

    /**
     * Treat new transformation as relative to current transformation.
     *
     * Internally: Initial transformation is set to old value, then current value is the
     * result of multiplication current by initial matrix.
     *
     */
    public void applyTransformationRelative(Matrix4 transformation) {
        initialTransformation.set(this.transformation);
        applyTransformation(transformation);
    }

    /**
     * Treat new transformation as relative to current transformation.
     *
     * Internally: Initial transformation is set to old value, then current value is the
     * result of multiplication current by initial matrix.
     *
     */
    public void applyTransformationRelative(Matrix3 transformation) {
        initialTransformation.set(this.transformation);
        applyTransformation(transformation);
    }


    /**
     * Multiplies initial state of transformation by provided matrix4 and sets current value.
     */
    public void applyTransformation(Matrix4 transformation) {
        this.transformation.set(transformation).mul(initialTransformation);
        setDirty();
    }

    /**
     * Multiplies initial state of transformation by provided matrix4 and sets current value.
     */
    public void applyTransformation(Matrix3 transformation) {
        this.transformation.set(transformation).mul(initialTransformation);
        setDirty();
    }

    /**
     * Sets current transformation matrix to provided matrix4.
     */
    public void setTransformationRaw(Matrix4 transformation) {
        this.transformation.set(transformation);
        setDirty();
    }

    /**
     * Sets current transformation matrix to provided matrix3.
     */
    public void setTransformationRaw(Matrix3 transformation) {
        this.transformation.set(transformation);
        setDirty();
    }

    /**
     * Sets current transformation matrix to provided matrix4.
     */
    public void setInitialTransformationRaw(Matrix4 transformation) {
        this.initialTransformation.set(transformation);
    }

    /**
     * Sets current transformation matrix to provided matrix3.
     */
    public void setInitialTransformationRaw(Matrix3 transformation) {
        this.initialTransformation.set(transformation);
    }

    public void idt() {
        initialTransformation.idt();
        transformation.idt();
        setDirty();
    }

    private void setDirty() {
        dirty = true;
    }

    public Matrix4 getTransformation() {
        return transformation;
    }

    public Matrix4 getInitialTransformation() {
        return initialTransformation;
    }

    public Matrix4 getInvTransformation() {
        if (dirty) {
            invTransformation.set(transformation).inv();
        }
        return invTransformation;
    }
}
