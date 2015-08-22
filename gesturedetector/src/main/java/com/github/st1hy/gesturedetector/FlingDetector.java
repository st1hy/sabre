package com.github.st1hy.gesturedetector;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

//TODO
/**
 * Detects translation events.
 * <p/>
 * Calls {@link Listener#onFling(PointF, float, Direction)} when appropriate.
 * <p/>
 * {@link Options.Event#TRANSLATE} enables or disables this detector.
 */
public class FlingDetector implements GestureDetector {
    protected final Listener listener;
    protected final Options options;

    /**
     * Constructs new {@link FlingDetector}.
     *
     * @param listener Listener to be called when events happen.
     * @param options  Options for controlling behavior of this detector.
     * @throws NullPointerException if listener of options are null.
     */
    public FlingDetector(Listener listener, Options options) {
        if (listener == null) throw new NullPointerException("Listener cannot be null");
        if (options == null) throw new NullPointerException("Options cannot be null");
        this.listener = listener;
        this.options = options.clone();
    }

    public interface Listener {
        void onFling(PointF startPoint, float velocity, Direction direction);
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
