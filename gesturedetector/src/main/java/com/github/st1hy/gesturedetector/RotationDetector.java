package com.github.st1hy.gesturedetector;

import android.graphics.PointF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static com.github.st1hy.gesturedetector.Options.Constant.ROTATION_START_THRESHOLD;

//TODO
/**
 * Listens for translation events.
 * <p/>
 * Calls {@link Listener#onRotate(GestureEventState, PointF, double, double)} when appropriate.
 * <p/>
 * {@link Options.Event#TRANSLATE} enables or disables this detector.
 */
public class RotationDetector implements GestureDetector {
    protected final Listener listener;
    protected final Options options;
    protected PointF centerPoint = new PointF();
    protected double rotation;
    protected double deltaRotation;
    protected boolean isEventValid = false, inProgress = false;
    protected GestureEventState currentState = GestureEventState.ENDED;
    /**
     * Indexed by pointer id.
     */
    protected final SparseArray<PointF> previousPoints = new SparseArray<>();

    /**
     * Constructs new {@link RotationDetector}.
     *
     * @param listener Listener to be called when events happen.
     * @param options  Options for controlling behavior of this detector.
     * @throws NullPointerException if listener of options are null.
     */
    public RotationDetector(Listener listener, Options options) {
        if (listener == null) throw new NullPointerException("Listener cannot be null");
        if (options == null) throw new NullPointerException("Options cannot be null");
        this.listener = listener;
        this.options = options.clone();
    }

    public interface Listener {
        /**
         * Called when rotation is detected. Only received when {@link Options.Event#ROTATE} is set in {@link Options}.
         *
         * @param state       state of event. Can be either {@link GestureEventState#STARTED} when {@link Options.Constant#ROTATION_START_THRESHOLD} is first reached, {@link GestureEventState#ENDED} when rotation ends or {@link GestureEventState#IN_PROGRESS}.
         * @param centerPoint point of reference for rotation
         * @param rotation    rotation in degrees since the beginning of the gesture in degrees.
         * @param delta       Relative rotation since the last call of onRotate in degrees.
         */
        void onRotate(GestureEventState state, PointF centerPoint, double rotation, double delta);
    }

    @Override
    public void invalidate() {
        isEventValid = false;
        inProgress = false;
        if (!GestureEventState.ENDED.equals(currentState)) {
            notifyListener(GestureEventState.ENDED);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!options.isEnabled(Options.Event.ROTATE)) return false;
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
        if (currentState != GestureEventState.ENDED) notifyListener(GestureEventState.ENDED);
        calculateCenter(event);
        return true;
    }

    protected boolean onActionUp(MotionEvent event) {
        if (!isEventValid) return false;
        invalidate();
        return true;
    }

    protected boolean onActionMove(MotionEvent event) {
        if (!isEventValid) return false;
        calculateRotation(event);
        if (!isEventValid) return false;
        if (currentState == GestureEventState.ENDED && (inProgress || rotation > options.get(ROTATION_START_THRESHOLD))) {
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
        listener.onRotate(currentState, point, rotation, deltaRotation);
    }

    protected void calculateRotation(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            inProgress = false;
            rotation = 0;
            deltaRotation = 0;
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
        centerPoint.set(centerX, centerY);

        double distance = 0;
        for (int i = 0; i < pointsCount; i++) {
            float dx = event.getX(i) - centerX;
            float dy = event.getY(i) - centerY;
            float tan = dy / dx;
            double angle = Math.atan(tan);

            distance += distance(dx, dy);
        }
//        currentDistance = distance / pointsCount;
//        scale = (float) (currentDistance / distanceStart);
    }

    protected void calculateCenter(MotionEvent event) {
        float centerX = 0;
        float centerY = 0;
        int pointsCount = event.getPointerCount();
        for (int i = 0; i < pointsCount; i++) {
            float x = event.getX(i);
            float y = event.getY(i);
            setPreviousPoint(i, x, y);
            centerX += event.getX(i);
            centerY += event.getY(i);
        }
        centerX /= pointsCount;
        centerY /= pointsCount;
        centerPoint.set(centerX, centerY);
        rotation = 0;
    }

    protected void setPreviousPoint(int pointerIndex, float x, float y) {
        PointF pointF = previousPoints.get(pointerIndex);
        if (pointF == null) {
            pointF = new PointF(x, y);
            previousPoints.put(pointerIndex, pointF);
        } else {
            pointF.set(x,y);
        }
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
