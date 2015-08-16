package com.github.st1hy.gesturedetector;

import android.graphics.PointF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.github.st1hy.gesturedetector.GestureListener.State;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static com.github.st1hy.gesturedetector.Options.Constant.TRANSLATION_START_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Flag.TRANSLATION_MULTITOUCH;
import static com.github.st1hy.gesturedetector.Options.Flag.TRANSLATION_STRICT_ONE_FINGER;

/**
 * Listens for translation events.
 * <p/>
 * Calls {@link GestureListener#onTranslate(State, float, float, float, float, double)} when appropriate.
 * <p/>
 * {@link Options.Event#TRANSLATE} enables or disables this detector.
 */
class TranslationDetector implements GestureDetector {
    private final GestureListener listener;
    private final Options options;
    private int eventStartPointerId;
    private float startX, startY, dx, dy;
    private final SparseArray<PointF> startFingerPositions = new SparseArray<>(); //Indexed by pointer id
    private double currentDistance;
    private boolean isEventValid = false, isTranslationStarted = false;
    private State currentState = State.ENDED;

    TranslationDetector(GestureListener listener, Options options) {
        this.listener = listener;
        this.options = options;
    }

    @Override
    public void invalidate() {
        isEventValid = false;
        isTranslationStarted = false;
        if (!State.ENDED.equals(currentState)) {
            notifyListener(State.ENDED);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!options.isEnabled(Options.Event.TRANSLATE)) return false;
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
        eventStartPointerId = getPointerId(event);
        int pointerIndex = event.getActionIndex();
        startX = event.getX(pointerIndex);
        startY = event.getY(pointerIndex);
        setStartAndGet(startFingerPositions, eventStartPointerId, startX, startY);
        isEventValid = true;
        return true;
    }

    private boolean onActionUp(MotionEvent event) {
        if (!isEventValid || !isTranslationStarted) return false;
        calculatePosition(event);
        if (!isEventValid) return false;
        invalidate();
        return true;
    }

    private boolean onActionMove(MotionEvent event) {
        if (!isEventValid) return false;
        calculatePosition(event);
        if (!isEventValid) return false;
        if (!isTranslationStarted && currentDistance > options.get(TRANSLATION_START_THRESHOLD)) {
            isTranslationStarted = true;
            notifyListener(State.STARTED);
        } else if (isTranslationStarted) {
            notifyListener(State.IN_PROGRESS);
        }
        return true;
    }

    private void notifyListener(State state) {
        currentState = state;
        listener.onTranslate(currentState, startX, startY, dx, dy, currentDistance);
    }

    private void calculatePosition(MotionEvent event) {
        if (options.getFlag(TRANSLATION_MULTITOUCH) && !options.getFlag(TRANSLATION_STRICT_ONE_FINGER)) {
            int dx = 0;
            int dy = 0;
            int maxPointers = event.getPointerCount();
            for (int i = 0; i < maxPointers; i++) {
                int pointerId = event.getPointerId(i);
                if (pointerId < 0)
                    throw new IllegalStateException("Pointer index lack pointer id!");
                PointF point = startFingerPositions.get(pointerId);
                dx += event.getX(i) - point.x;
                dy += event.getY(i) - point.y;
            }
            this.dx = dx / maxPointers;
            this.dy = dy / maxPointers;
        } else {
            int pointerIndex = event.findPointerIndex(eventStartPointerId);
            if (pointerIndex == -1) {
                invalidate();
                return;
            }
            float currentX = event.getX(pointerIndex);
            float currentY = event.getY(pointerIndex);
            dx = currentX - startX;
            dy = currentY - startY;
        }
        currentDistance = Math.sqrt(dx * dx + dy * dy);
    }

    private boolean onActionPointerDown(MotionEvent event) {
        if (!isEventValid || !isMultitouchAware()) return false;
        if (options.getFlag(TRANSLATION_STRICT_ONE_FINGER)) {
            invalidate();
        } else if (options.getFlag(TRANSLATION_MULTITOUCH)) {
            int pointerIndex = event.getActionIndex();
            int pointerId = event.getPointerId(pointerIndex);
            float currentX = event.getX(pointerIndex);
            float currentY = event.getY(pointerIndex);
            setStartAndGet(startFingerPositions, pointerId, currentX, currentY);
        }
        return true;
    }

    private PointF setStartAndGet(SparseArray<PointF> source, int pointerId, float startX, float startY) {
        PointF startPoint = source.get(pointerId);
        if (startPoint == null) {
            source.put(pointerId, new PointF(startX, startY));
        } else {
            startPoint.set(startX, startY);
        }
        return startPoint;
    }

    private boolean onActionPointerUp(MotionEvent event) {
        return isEventValid && !options.getFlag(TRANSLATION_MULTITOUCH) && getPointerId(event) == eventStartPointerId && onActionUp(event);
    }

    private boolean isMultitouchAware() {
        return options.getFlag(TRANSLATION_MULTITOUCH) || options.getFlag(TRANSLATION_STRICT_ONE_FINGER);
    }

    private static int getPointerId(MotionEvent event) {
        return event.getPointerId(event.getActionIndex());
    }
}