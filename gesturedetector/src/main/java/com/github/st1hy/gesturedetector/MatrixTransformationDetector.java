package com.github.st1hy.gesturedetector;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;

/**
 * Calculates transformation matrix based on user touch input.
 */
public class MatrixTransformationDetector implements GestureDetector {
    protected Listener listener;
    protected boolean isEventValid = false, inProgress = false;
    protected GestureEventState currentState = GestureEventState.ENDED;
    /**
     * Indexed by pointer id.
     */
    protected final SparseArray<PointF> startPoints = new SparseArray<>();
    protected Matrix matrix = new Matrix();
    protected float[] poly = new float[16];

    public MatrixTransformationDetector(Listener listener) {
        if (listener == null) throw new NullPointerException("Listener cannot be null");
        this.listener = listener;
    }

    @Override
    public void invalidate() {
        isEventValid = false;
        inProgress = false;
        if (!GestureEventState.ENDED.equals(currentState)) {
            notifyListener(GestureEventState.ENDED);
        }
    }

    public interface Listener {
        void onMatrix(GestureEventState state, Matrix currentTransformation);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case ACTION_DOWN:
                return onActionDown(event);
            case ACTION_UP:
                return onActionUp(event);
            case ACTION_MOVE:
                return onActionMove(event);
            case ACTION_POINTER_DOWN:
                return onActionPointerDown(event);
            case ACTION_POINTER_UP:
                return onActionPointerUp(event);
        }
        return false;
    }

    protected boolean onActionDown(MotionEvent event) {
        setStartPoints(event);
        isEventValid = true;
        return true;
    }

    protected boolean onActionPointerDown(MotionEvent event) {
        if (event.getPointerCount() > 4) invalidate();
        if (!isEventValid) return false;
        if (currentState != GestureEventState.ENDED) notifyListener(GestureEventState.ENDED);
        setStartPoints(event);
        return true;
    }

    protected boolean onActionUp(MotionEvent event) {
        if (!isEventValid) return false;
        invalidate();
        return true;
    }

    protected boolean onActionMove(MotionEvent event) {
        if (!isEventValid) return false;
        computeMatrix(event);
        if (!isEventValid) return false;
        if (currentState == GestureEventState.ENDED) {
            notifyListener(GestureEventState.STARTED);
        } else {
            notifyListener(GestureEventState.IN_PROGRESS);
        }
        return true;
    }

    protected void notifyListener(GestureEventState state) {
        currentState = state;
        listener.onMatrix(currentState, matrix);
    }

    protected void computeMatrix(MotionEvent event) {
        int pointsCount = event.getPointerCount();
        if (pointsCount < 0 || pointsCount > 4) {
            inProgress = false;
            matrix.reset();
            return;
        }
        int polySize =  pointsCount * 2;
        for (int i = 0; i < pointsCount; i++) {
            int pointerId = event.getPointerId(i);
            PointF src = startPoints.get(pointerId);
            int xIndex = i * 2;
            int yIndex = xIndex + 1;
            poly[xIndex] = src.x;
            poly[yIndex] = src.y;
            poly[xIndex + polySize] = event.getX(i);
            poly[yIndex + polySize] = event.getY(i);
        }
        matrix.setPolyToPoly(poly,0,poly,polySize, pointsCount);
    }

    protected void setStartPoints(MotionEvent event) {
        int pointsCount = event.getPointerCount();
        for (int i = 0; i < pointsCount; i++) {
            int pointerId = event.getPointerId(i);
            setStartPoint(pointerId, event.getX(i), event.getY(i));
        }
    }

    protected void setStartPoint(int pointerIndex, float x, float y) {
        PointF pointF = startPoints.get(pointerIndex);
        if (pointF == null) {
            pointF = new PointF(x, y);
            startPoints.put(pointerIndex, pointF);
        } else {
            pointF.set(x,y);
        }
    }

    protected boolean onActionPointerUp(MotionEvent event) {
        if (!isEventValid) return false;
        if (currentState != GestureEventState.ENDED) notifyListener(GestureEventState.ENDED);
        setStartPoints(event);
        return true;
    }
}
