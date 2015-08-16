package com.github.st1hy.gesturedetector;

import android.graphics.PointF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.github.st1hy.gesturedetector.GestureListener.State;

import java.util.EnumSet;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static com.github.st1hy.gesturedetector.Options.Constant.SCALE_START_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Constant.TRANSLATION_START_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Flag.TRANSLATION_MULTITOUCH;
import static com.github.st1hy.gesturedetector.Options.Flag.TRANSLATION_STRICT_ONE_FINGER;

/**
 * Listens for scale events.
 * <p/>
 * Calls {@link GestureListener#onScale(State, PointF, float)} when appropriate.
 * <p/>
 * {@link Options.Event#SCALE} enables or disables this detector.
 */
class ScaleDetector implements GestureDetector {
    private final GestureListener listener;
    private final Options options;
    private final SparseArray<PointF> startFingerPositions = new SparseArray<>(); //Indexed by pointer id
    private PointF centerPoint = new PointF();
    private float scale;
    private double distanceStart, currentDistance;
    private boolean isEventValid = false;
    private State currentState = State.ENDED;

    ScaleDetector(GestureListener listener, Options options) {
        this.listener = listener;
        this.options = options;
    }

    @Override
    public void invalidate() {
        isEventValid = false;
        startFingerPositions.clear();
        if (!State.ENDED.equals(currentState)) {
            notifyListener(State.ENDED);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!options.isEnabled(Options.Event.SCALE)) return false;
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

    private boolean onActionDown(MotionEvent event) {
        registerStartingPoint(event);
        isEventValid = true;
        return true;
    }


    private boolean onActionPointerDown(MotionEvent event) {
        if (!isEventValid ) return false;
        registerStartingPoint(event);
        if (startFingerPositions.size() > 2) {
            notifyListener(State.ENDED);
        }
        calculateCenter();
        return true;
    }

    private void registerStartingPoint(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        setStartAndGet(startFingerPositions, pointerId, event.getX(pointerIndex), event.getY(pointerIndex));
    }

    private boolean onActionUp(MotionEvent event) {
        if (!isEventValid) return false;
        invalidate();
        return true;
    }

    private boolean onActionMove(MotionEvent event) {
        if (!isEventValid) return false;
        calculatePosition(event);
        if (!isEventValid) return false;
        if (currentState == State.ENDED && currentDistance > options.get(SCALE_START_THRESHOLD)) {
            notifyListener(State.STARTED);
        } else if (currentState != State.ENDED) {
            notifyListener(State.IN_PROGRESS);
        }
        return true;
    }

    private void notifyListener(State state) {
        currentState = state;
        PointF point = new PointF();
        point.set(centerPoint);
        listener.onScale(currentState, point, scale);
    }

    private void calculatePosition(MotionEvent event) {
        if (startFingerPositions.size() < 2) {
            currentDistance = 0;
            return;
        }
        float centerX = 0;
        float centerY = 0;
        int pointsCount = event.getPointerCount();
        for (int i = 0; i < pointsCount; i++) {
            centerX += event.getX(i);
            centerY += event.getY(i);
        }
        centerX /= pointsCount;
        centerY /= pointsCount;

        double distance = 0;
        for (int i = 0; i < pointsCount; i++) {
            float dx = event.getX(i) - centerX;
            float dy = event.getY(i) - centerY;
            distance += distance(dx, dy);
        }
        currentDistance = distance / pointsCount;
        scale = (float) (currentDistance / distanceStart);
    }

    private void calculateCenter() {
        float centerX = 0;
        float centerY = 0;
        int pointsCount = startFingerPositions.size();
        for (int i = 0; i < pointsCount; i++) {
            PointF point = startFingerPositions.valueAt(i);
            centerX += point.x;
            centerY += point.y;
        }
        centerX /= pointsCount;
        centerY /= pointsCount;
        centerPoint.set(centerX, centerY);

        double distance = 0;
        for (int i = 0; i < pointsCount; i++) {
            PointF point = startFingerPositions.valueAt(i);
            float dx = point.x - centerX;
            float dy = point.y - centerY;
            distance += distance(dx, dy);
        }
        this.distanceStart = distance / pointsCount;
    }

    private static double distance(float a, float b) {
        return Math.sqrt(a * a + b * b);
    }


    private PointF setStartAndGet(SparseArray<PointF> consumer, int pointerId, float startX, float startY) {
        PointF startPoint = consumer.get(pointerId);
        if (startPoint == null) {
            consumer.put(pointerId, new PointF(startX, startY));
        } else {
            startPoint.set(startX, startY);
        }
        return startPoint;
    }

    private boolean onActionPointerUp(MotionEvent event) {
        if (!isEventValid) return false;
        notifyListener(State.ENDED);
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        startFingerPositions.remove(pointerId);
        calculateCenter();
        return true;
    }
}