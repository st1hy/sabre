package com.github.st1hy.gesturedetector;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

//TODO

/**
 * Listens for translation events.
 * <p/>
 * Calls {@link Listener#onRotate(GestureEventState, PointF, float)} when appropriate.
 * <p/>
 * {@link Options.Event#TRANSLATE} enables or disables this detector.
 */
public class RotationDetector implements GestureDetector {
    protected final Listener listener;
    protected final Options options;

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
         * Called when rotation is detected. Only received when {@link Options.Event#TRANSLATE} is set in {@link Options}.
         *
         * @param state       state of event. Can be either {@link GestureEventState#STARTED} when {@link Options.Constant#ROTATION_START_THRESHOLD} is first reached, {@link GestureEventState#ENDED} when rotation ends or {@link GestureEventState#IN_PROGRESS}.
         * @param centerPoint point of reference for rotation
         * @param rotation    rotation in degrees since the beginning of the gesture
         */
        void onRotate(GestureEventState state, PointF centerPoint, float rotation);
    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
