package com.github.st1hy.gesturedetector;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static com.github.st1hy.gesturedetector.Options.Constant.TRANSLATION_START_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Flag.TRANSLATION_STRICT_ONE_FINGER;

/**
 * Detects translation events.
 * <p/>
 * Calls {@link Listener#onTranslate(GestureEventState, PointF, float, float, double)} when appropriate.
 * <p/>
 * {@link Options.Event#TRANSLATE} enables or disables this detector.
 */
public class TranslationDetector implements GestureDetector {
    protected final Listener listener;
    protected final Options options;
    protected PointF centerPoint = new PointF();
    protected float dx, dy;
    protected double currentDistance;
    protected boolean isEventValid = false, inProgress = false;
    protected GestureEventState currentState = GestureEventState.ENDED;

    /**
     * Constructs new {@link TranslationDetector}.
     *
     * @param listener Listener to be called when events happen.
     * @param options  Options for controlling behavior of this detector.
     * @throws NullPointerException if listener of options are null.
     */
    public TranslationDetector(Listener listener, Options options) {
        if (listener == null) throw new NullPointerException("Listener cannot be null");
        if (options == null) throw new NullPointerException("Options cannot be null");
        this.listener = listener;
        this.options = options.clone();
    }

    public interface Listener {
        /**
         * Is called when translation occurs. Require {@link Options.Event#TRANSLATE} to be set in {@link Options}.
         *
         * @param state      state of event. Can be either {@link GestureEventState#STARTED} when {@link Options.Constant#TRANSLATION_START_THRESHOLD} is first reached, {@link GestureEventState#ENDED} when translation ends or {@link GestureEventState#IN_PROGRESS}.
         * @param startPoint position on the finger when it was first pressed
         * @param dx         initial translation on x axis when event was triggered
         * @param dy         initial translation on y axis when event was triggered
         * @param distance   distance between start point and current point that triggered translation. Must be above {@link Options.Constant#TRANSLATION_START_THRESHOLD}.
         */
        void onTranslate(GestureEventState state, PointF startPoint, float dx, float dy, double distance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidate() {
        isEventValid = false;
        inProgress = false;
        if (!GestureEventState.ENDED.equals(currentState)) {
            notifyListener(GestureEventState.ENDED);
        }
    }

    /**
     * {@inheritDoc}
     */
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

    protected boolean onActionDown(MotionEvent event) {
        calculateCenter(event);
        isEventValid = true;
        return true;
    }

    protected boolean onActionPointerDown(MotionEvent event) {
        if (!isEventValid) return false;
        if (options.getFlag(TRANSLATION_STRICT_ONE_FINGER)) {
            invalidate();
        } else {
            if (currentState != GestureEventState.ENDED) notifyListener(GestureEventState.ENDED);
            calculateCenter(event);
        }
        return true;
    }

    protected boolean onActionUp(MotionEvent event) {
        if (!isEventValid) return false;
        invalidate();
        return true;
    }

    protected boolean onActionMove(MotionEvent event) {
        if (!isEventValid) return false;
        calculatePosition(event);
        if (!isEventValid) return false;
        if (currentState == GestureEventState.ENDED && (inProgress || currentDistance > options.get(TRANSLATION_START_THRESHOLD))) {
            inProgress = true;
            notifyListener(GestureEventState.STARTED);
        } else if (currentState != GestureEventState.ENDED) {
            notifyListener(GestureEventState.IN_PROGRESS);
        }
        return true;
    }

    protected void notifyListener(GestureEventState state) {
        currentState = state;
        PointF point = new PointF();
        point.set(centerPoint);
        listener.onTranslate(state, point, dx, dy, currentDistance);
    }

    protected void calculatePosition(MotionEvent event) {
        float centerX = 0;
        float centerY = 0;
        int pointsCount = event.getPointerCount();
        for (int i = 0; i < pointsCount; i++) {
            centerX += event.getX(i);
            centerY += event.getY(i);
        }
        centerX /= pointsCount;
        centerY /= pointsCount;

        dx = centerX - centerPoint.x;
        dy = centerY - centerPoint.y;
        currentDistance = distance(dx, dy);
    }

    protected void calculateCenter(MotionEvent event) {
        float centerX = 0;
        float centerY = 0;
        int pointsCount = event.getPointerCount();
        for (int i = 0; i < pointsCount; i++) {
            centerX += event.getX(i);
            centerY += event.getY(i);
        }
        centerX /= pointsCount;
        centerY /= pointsCount;
        centerPoint.set(centerX, centerY);
    }

    protected static double distance(float a, float b) {
        return Math.sqrt(a * a + b * b);
    }

    protected boolean onActionPointerUp(MotionEvent event) {
        if (!isEventValid) return false;
        if (currentState != GestureEventState.ENDED) notifyListener(GestureEventState.ENDED);
        calculateCenter(event);
        return true;
    }
}