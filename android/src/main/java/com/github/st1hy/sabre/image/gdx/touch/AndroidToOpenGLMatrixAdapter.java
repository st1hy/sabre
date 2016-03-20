package com.github.st1hy.sabre.image.gdx.touch;

import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Matrix3;
import com.github.st1hy.core.Matrix3ChangedListener;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.MatrixTransformationDetector;

/**
 * Transforms memory order of matrix from default android row-major order to openGL column-major order.
 */
public class AndroidToOpenGLMatrixAdapter implements MatrixTransformationDetector.Listener {
    private float[] valuesTemp = new float[9];
    private float[] valuesTempColumnMajor = new float[9];
    private final Matrix3 matrix3Temp = new Matrix3();
    private Matrix3ChangedListener dispatch;

    public AndroidToOpenGLMatrixAdapter(@Nullable Matrix3ChangedListener dispatch) {
        this.dispatch = dispatch;
    }

    public AndroidToOpenGLMatrixAdapter() {
        this(null);
    }

    public void setDispatch(@Nullable Matrix3ChangedListener dispatch) {
        this.dispatch = dispatch;
    }

    @Override
    public void onMatrix(@NonNull GestureEventState state, @NonNull Matrix matrix) {
        Matrix3ChangedListener dispatch = this.dispatch;
        if (dispatch == null) return;
        matrix.getValues(valuesTemp);
        changeMemoryOrder(valuesTemp, valuesTempColumnMajor);
        matrix3Temp.set(valuesTempColumnMajor);
        dispatch.onMatrix3Changed(from(state), matrix3Temp);
    }

    public void reset() {
        if (dispatch != null) dispatch.onMatrix3Reset();
    }

    private static void changeMemoryOrder(float[] rowMajorInput, float[] columnMajorOutput) {
        columnMajorOutput[0] = rowMajorInput[0];
        columnMajorOutput[1] = rowMajorInput[3];
        columnMajorOutput[2] = rowMajorInput[6];
        columnMajorOutput[3] = rowMajorInput[1];
        columnMajorOutput[4] = rowMajorInput[4];
        columnMajorOutput[5] = rowMajorInput[7];
        columnMajorOutput[6] = rowMajorInput[2];
        columnMajorOutput[7] = rowMajorInput[5];
        columnMajorOutput[8] = rowMajorInput[8];
    }

    private static Matrix3ChangedListener.State from(GestureEventState state) {
        switch (state) {
            case STARTED:
                return Matrix3ChangedListener.State.STARTED;
            case IN_PROGRESS:
                return Matrix3ChangedListener.State.IN_PROGRESS;
            case ENDED:
                return Matrix3ChangedListener.State.ENDED;
            default:
                throw new UnsupportedOperationException();
        }
    }
}