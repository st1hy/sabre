package com.github.st1hy.sabre.gdx.image;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix3;
import com.github.st1hy.core.ImageGdxCore;
import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.MatrixTransformationDetector;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.gesturedetector.SimpleGestureListener;

public class ImageOnTouchListener extends SimpleGestureListener implements GestureDetector {
    private final GestureDetector dispatch;
    private final ImageGdxCore core;
    private final float[] valuesTemp = new float[9];
    private final float[] valuesTempColumnMajor = new float[9];
    private final Matrix3 startingMatrix = new Matrix3();
    private final Matrix3 matrix3Temp = new Matrix3();
    private final Matrix3 matrix3Multiplied = new Matrix3();

    public ImageOnTouchListener(ImageGdxCore core) {
        this.core = core;
        Options options = new Options();
        options.set(Options.Constant.MATRIX_MAX_POINTERS_COUNT, 3);
        options.setFlag(Options.Flag.MATRIX_OPEN_GL_COMPATIBILITY, true);
        options.setEnabled(Options.Event.MATRIX_TRANSFORMATION, true);
        dispatch = new MatrixTransformationDetector(this, options);
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
        core.getTransformation().set(matrix3Multiplied);
        Gdx.graphics.requestRendering();
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

    @Override
    public void invalidate() {
        dispatch.invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return dispatch.onTouch(v, event);
    }
}
