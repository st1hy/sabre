package com.github.st1hy.sabre.image.gdx.touch.transform;

import android.graphics.Matrix;
import android.support.annotation.NonNull;

import com.badlogic.gdx.math.Matrix3;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.MatrixTransformationDetector;

/**
 * Transforms memory order of matrix from default android row-major order to openGL column-major order.
 */
public class AndroidToOpenGLMatrixAdapter implements MatrixTransformationDetector.Listener {
    private final Matrix3ChangedListener dispatch;
    private float[] valuesTemp = new float[9];
    private float[] valuesTempColumnMajor = new float[9];
    private Matrix3 startingMatrix = new Matrix3();
    private Matrix3 matrix3Temp = new Matrix3();
    private Matrix3 matrix3Multiplied = new Matrix3();

    public AndroidToOpenGLMatrixAdapter(@NonNull Matrix3ChangedListener dispatch) {
        this.dispatch = dispatch;
    }

    public void reset() {
        valuesTemp = new float[9];
        valuesTempColumnMajor = new float[9];
        startingMatrix = new Matrix3();
        matrix3Temp = new Matrix3();
        matrix3Multiplied = new Matrix3();
    }

    @Override
    public void onMatrix(GestureEventState state, Matrix matrix) {
        if (state.equals(GestureEventState.STARTED)) {
            startingMatrix.set(matrix3Multiplied);
        }
        matrix.getValues(valuesTemp);
        changeMemoryOrder(valuesTemp, valuesTempColumnMajor);
        matrix3Temp.set(valuesTempColumnMajor);
        matrix3Multiplied.set(matrix3Temp);
        matrix3Multiplied.mul(startingMatrix);
        dispatch.onMatrix3Changed(matrix3Multiplied);
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
}
