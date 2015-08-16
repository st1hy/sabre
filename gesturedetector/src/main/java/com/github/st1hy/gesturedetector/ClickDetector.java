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
 * Listens for click events.
 * Calls {@link GestureListener#onClick(PointF)}, {@link GestureListener#onLongPressed(PointF)} or {@link GestureListener#onDoubleClick(PointF)} when appropriate.
 * <p>
 * To control which events are being delivered use {@link Options}. If option {@link Flag#IGNORE_CLICK_EVENT_ON_GESTURES} is set it will filter out clicks that are part of more complicated gestures.
 * <p>
 * When listening also for double click events, adds delay for second press event to occur.
 * If it doesn't it triggers {@link GestureListener#onClick(PointF)}
 */
class ClickDetector extends SimpleGestureListener implements GestureDetector {
    private final GestureListener listener;
    private final Options options;
    private long pressedTimestamp, previousClickTimestamp;
    private int eventStartPointerId;
    private PointF startPoint;
    private boolean isEventValid = false;
    private int clickCount = 0;
    private final Handler handler;
    private final Runnable delayedClick = new Runnable() {
        @Override
        public void run() {
            if (options.isEnabled(CLICK)) {
                listener.onClick(startPoint);
                invalidate();
            }
        }
    };
    private final Runnable delayedLongPress = new Runnable() {
        @Override
        public void run() {
            if (options.isEnabled(LONG_PRESS)) {
                listener.onLongPressed(startPoint);
                invalidate();
            }
        }
    };
    private final Runnable doubleClickTimeout = new Runnable() {
        @Override
        public void run() {
            if (options.isEnabled(DOUBLE_CLICK)) {
                invalidate();
            }
        }
    };

    ClickDetector(GestureListener listener, Options options) {
        this.listener = listener;
        this.options = options;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void invalidate() {
        isEventValid = false;
        clickCount = 0;
        handler.removeCallbacks(delayedClick);
        handler.removeCallbacks(delayedLongPress);
        handler.removeCallbacks(doubleClickTimeout);
    }

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

    private boolean onActionDown(MotionEvent event) {
        previousClickTimestamp = pressedTimestamp;
        pressedTimestamp = event.getEventTime();
        eventStartPointerId = getPointerId(event);
        int pointerIndex = event.getActionIndex();
        startPoint = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));
        isEventValid = true;
        if (options.isEnabled(DOUBLE_CLICK)) clickCount++;
        if (options.isEnabled(LONG_PRESS)) {
            handler.postDelayed(delayedLongPress, options.get(LONG_PRESS_TIME_MS));
        }
        handler.removeCallbacks(delayedClick);
        return true;
    }

    private boolean onActionUp(MotionEvent event) {
        if (!isEventValid) return false;
        if (options.getFlag(IGNORE_CLICK_EVENT_ON_GESTURES)) {
            //Other finger is up than was down in the first place.
            if (getPointerId(event) != eventStartPointerId) {
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

    @Override
    public void onTranslate(State state, PointF startPoint, float dx, float dy, double distance) {
        if (isEventValid && State.STARTED.equals(state) && options.getFlag(IGNORE_CLICK_EVENT_ON_GESTURES)) {
            invalidate();
        }
    }

    private boolean onActionMove(MotionEvent event) {
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

    private boolean onActionPointerDown(MotionEvent event) {
        if (isEventValid && options.getFlag(IGNORE_CLICK_EVENT_ON_GESTURES)) {
            //Multitouch gesture started.
            invalidate();
            return true;
        }
        return false;
    }

    private static int getPointerId(MotionEvent event) {
        return event.getPointerId(event.getActionIndex());
    }

    private static double getDistance(PointF startPoint, float endX, float endY) {
        double a = endX - startPoint.x;
        double b = endY - startPoint.y;
        return Math.sqrt(a * a + b * b);
    }

    private boolean isListeningForSomething() {
        return options.isEnabled(CLICK, DOUBLE_CLICK, LONG_PRESS);
    }
}
