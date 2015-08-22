package com.github.st1hy.gesturedetector;

import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import com.github.st1hy.gesturedetector.Options.Flag;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static com.github.st1hy.gesturedetector.Options.Constant.DOUBLE_CLICK_TIME_LIMIT;
import static com.github.st1hy.gesturedetector.Options.Constant.LONG_PRESS_TIME_MS;
import static com.github.st1hy.gesturedetector.Options.Constant.TRANSLATION_START_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Event.CLICK;
import static com.github.st1hy.gesturedetector.Options.Event.DOUBLE_CLICK;
import static com.github.st1hy.gesturedetector.Options.Event.LONG_PRESS;
import static com.github.st1hy.gesturedetector.Options.Event.TRANSLATE;
import static com.github.st1hy.gesturedetector.Options.Flag.IGNORE_CLICK_EVENT_ON_GESTURES;

/**
 * Detects click events.
 * <p/>
 * Calls {@link Listener#onClick(PointF)}, {@link Listener#onLongPressed(PointF)} or {@link Listener#onDoubleClick(PointF)} when appropriate.
 * <p/>
 * To control which events are being delivered use {@link Options}. If option {@link Flag#IGNORE_CLICK_EVENT_ON_GESTURES} is set it will filter out clicks that are part of more complicated gestures.
 * <p/>
 * When listening also for double click events, adds delay for second press event to occur.
 * If it doesn't it triggers {@link Listener#onClick(PointF)}
 * <p/>
 * This implementation must receive events on {@link TranslationDetector.Listener#onTranslate(GestureEventState, PointF, float, float, double)} if both {@link Options.Event#TRANSLATE} and {@link Flag#IGNORE_CLICK_EVENT_ON_GESTURES} are enabled.
 */
public class ClickDetector implements TranslationDetector.Listener, GestureDetector {
    protected final Listener listener;
    protected final Options options;
    protected long pressedTimestamp, previousClickTimestamp;
    protected int eventStartPointerId;
    protected PointF startPoint;
    protected boolean isEventValid = false;
    protected int clickCount = 0;
    protected final Handler handler;
    protected final Runnable delayedClick = new Runnable() {
        @Override
        public void run() {
            if (options.isEnabled(CLICK)) {
                listener.onClick(startPoint);
                invalidate();
            }
        }
    };
    protected final Runnable delayedLongPress = new Runnable() {
        @Override
        public void run() {
            if (options.isEnabled(LONG_PRESS)) {
                listener.onLongPressed(startPoint);
                invalidate();
            }
        }
    };
    protected final Runnable doubleClickTimeout = new Runnable() {
        @Override
        public void run() {
            if (options.isEnabled(DOUBLE_CLICK)) {
                invalidate();
            }
        }
    };

    /**
     * Click detector provides listener information about click events on {@link Listener}
     *
     * @param listener Listener to be called when events happen.
     * @param options  Options for controlling behavior of this detector.
     * @throws NullPointerException if listener of options are null.
     */
    public ClickDetector(Listener listener, Options options) {
        if (listener == null) throw new NullPointerException("Listener cannot be null");
        if (options == null) throw new NullPointerException("Options cannot be null");
        this.listener = listener;
        this.options = options.clone();
        handler = new Handler(Looper.getMainLooper());
    }

    public interface Listener {
        /**
         * Called when click event is detected.
         *
         * @param startPoint Point where click event was detected.
         */
        void onClick(PointF startPoint);

        /**
         * Called when long press event is detected.
         *
         * @param startPoint Point where long press event was detected.
         */
        void onLongPressed(PointF startPoint);

        /**
         * Called when double click event is detected.
         *
         * @param startPoint Point where double click event was detected.
         */
        void onDoubleClick(PointF startPoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidate() {
        isEventValid = false;
        clickCount = 0;
        handler.removeCallbacks(delayedClick);
        handler.removeCallbacks(delayedLongPress);
        handler.removeCallbacks(doubleClickTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isListeningForSomething()) return false;
        switch (event.getActionMasked()) {
            case ACTION_DOWN:
                return onActionDown(event);
            case ACTION_UP:
                return onActionUp(event);
            case ACTION_MOVE:
                return onActionMove(event);
            case ACTION_POINTER_DOWN:
                return onActionPointerDown(event);
        }
        return false;
    }

    protected boolean onActionDown(MotionEvent event) {
        previousClickTimestamp = pressedTimestamp;
        pressedTimestamp = event.getEventTime();
        int pointerIndex = event.getActionIndex();
        eventStartPointerId = event.getPointerId(pointerIndex);
        startPoint = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));
        isEventValid = true;
        if (options.isEnabled(DOUBLE_CLICK)) clickCount++;
        if (options.isEnabled(LONG_PRESS)) {
            handler.postDelayed(delayedLongPress, options.get(LONG_PRESS_TIME_MS));
        }
        handler.removeCallbacks(delayedClick);
        return true;
    }

    protected boolean onActionUp(MotionEvent event) {
        if (!isEventValid) return false;
        if (options.getFlag(IGNORE_CLICK_EVENT_ON_GESTURES)) {
            //Other finger is up than was down in the first place.
            if (event.getPointerId(event.getActionIndex()) != eventStartPointerId) {
                invalidate();
                return false;
            }
        }
        if (options.isEnabled(DOUBLE_CLICK)) {
            if (clickCount == 2) {
                long timeSinceFirstClick = event.getEventTime() - previousClickTimestamp;
                if (timeSinceFirstClick < options.get(DOUBLE_CLICK_TIME_LIMIT)) {
                    listener.onDoubleClick(startPoint);
                }
                invalidate();
            } else {
                long timePressed = event.getEventTime() - pressedTimestamp;
                long delay = options.get(DOUBLE_CLICK_TIME_LIMIT) + 1 - timePressed;
                if (options.isEnabled(CLICK)) {
                    handler.postDelayed(delayedClick, delay);
                } else {
                    handler.postDelayed(doubleClickTimeout, delay);
                }
            }
        } else if (options.isEnabled(CLICK)) {
            long timePressed = event.getEventTime() - pressedTimestamp;
            if (timePressed < options.get(LONG_PRESS_TIME_MS)) {
                listener.onClick(startPoint);
            }
            invalidate();
        } else {
            invalidate();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTranslate(GestureEventState state, PointF startPoint, float dx, float dy, double distance) {
        if (isEventValid && GestureEventState.STARTED.equals(state) && options.getFlag(IGNORE_CLICK_EVENT_ON_GESTURES)) {
            invalidate();
        }
    }

    protected boolean onActionMove(MotionEvent event) {
        //If translation detection is disabled we need to detect it ourselves unless we don't care
        if (isEventValid && options.getFlag(IGNORE_CLICK_EVENT_ON_GESTURES) && !options.isEnabled(TRANSLATE)) {
            int pointerIndex = event.findPointerIndex(eventStartPointerId);
            if (pointerIndex == -1) {
                invalidate();
                return false;
            }
            float x = event.getX(pointerIndex);
            float y = event.getY(pointerIndex);
            //We detect movement above threshold - its no longer a click but a translation.
            if (getDistance(startPoint, x, y) > options.get(TRANSLATION_START_THRESHOLD)) {
                invalidate();
            }
            return true;
        }
        return false;
    }

    protected boolean onActionPointerDown(MotionEvent event) {
        if (isEventValid && options.getFlag(IGNORE_CLICK_EVENT_ON_GESTURES)) {
            //Multitouch gesture started.
            invalidate();
            return true;
        }
        return false;
    }

    protected static double getDistance(PointF startPoint, float endX, float endY) {
        double a = endX - startPoint.x;
        double b = endY - startPoint.y;
        return Math.sqrt(a * a + b * b);
    }

    protected boolean isListeningForSomething() {
        return options.isEnabled(CLICK) || options.isEnabled(DOUBLE_CLICK) || options.isEnabled(LONG_PRESS);
    }
}
