package com.github.st1hy.coregdx;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public interface Transformable {
    void applyTransformation(TouchEventState state, Matrix3 matrix3);

    void resetTransformation();

    AppliedTransformation TRANSFORM = new AppliedTransformation() {
        @Override
        public void apply(Transformation transformation, TouchEventState state, Matrix3 matrix3) {
            if (state == TouchEventState.STARTED) {
                transformation.applyTransformationRelative(matrix3);
            } else {
                transformation.applyTransformation(matrix3);
            }
        }
        @Override
        public void apply(Transformation transformation, TouchEventState state, Matrix4 matrix4) {
            if (state == TouchEventState.STARTED) {
                transformation.applyTransformationRelative(matrix4);
            } else {
                transformation.applyTransformation(matrix4);
            }
        }
    };

    interface AppliedTransformation {
        void apply(Transformation transformation, TouchEventState state, Matrix3 matrix3);
        void apply(Transformation transformation, TouchEventState state, Matrix4 matrix3);
    }
}
