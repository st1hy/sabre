package com.github.st1hy.gesturedetector;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

/**
 * Listens for click events.
 * Calls {@link GestureListener#onClick()}, {@link GestureListener#onLongPressed()} ()} or {@link GestureListener#onDoubleClick()} when appropriate.
 * <p/>
 * If option {@link Options#ignoreClickEventOnGestures} is set it will filter out clicks that are part of more complicated gestures.
 * <p/>
 * When listening also for double click events, adds delay for second press event to occur.
 * If it doesn't it triggers {@link GestureListener#onClick()}
 */
class ClickDetector implements View.OnTouchListener {
    private static final int MS_TO_NS = 1_000_000;
    private static final boolean inDebug = Config.DEBUG;
    private static final String TAG = "ClickDetector";
    private final GestureListener listener;
    private Options options;
    private long pressedTimestamp, previousClickTimestamp;
    private int eventStartPointerIndex;
    private float startX, startY;
    private boolean isEventValid = false;
    private int clickCount = 0;
    private final Handler handler;
    private final Runnable delayedClick = new Runnable() {
        @Override
        public void run() {
            if (options.isListenForClick()) {
                listener.onClick();
                invalidate();
            }
        }
    };
    private final Runnable delayedLongPress = new Runnable() {
        @Override
        public void run() {
            if (options.isListenForLongClick()) {
                listener.onLongPressed();
                invalidate();
            }
        }
    };
    private final Runnable doubleClickTimeout = new Runnable() {
        @Override
        public void run() {
            if (options.isListenForDoubleClick()) {
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
    public boolean onTouch(View v, MotionEvent event) {
        if (!isListeningForSomething()) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return onActionDown(event);
            case MotionEvent.ACTION_UP:
                return onActionUp(event);
            case MotionEvent.ACTION_MOVE:
                return onActionMove(event);
            case MotionEvent.ACTION_POINTER_DOWN:
                return onActionPointerDown(event);
        }
        return false;
    }

    private boolean onActionDown(MotionEvent event) {
        previousClickTimestamp = pressedTimestamp;
        pressedTimestamp = System.nanoTime();
        eventStartPointerIndex = getPointerIndex(event);
        startX = event.getX();
        startY = event.getY();
        isEventValid = true;
        if (options.isListenForDoubleClick()) clickCount++;
        if (options.isListenForLongClick()) {
            handler.postDelayed(delayedLongPress, options.getLongPressTimeMs());
        }
        handler.removeCallbacks(delayedClick);
        return true;
    }

    private boolean onActionUp(MotionEvent event) {
        if (!isEventValid) return false;
        if (options.isIgnoreClickEventOnGestures()) {
            //Other finger is up than was down in the first place.
            if (getPointerIndex(event) != eventStartPointerIndex) {
                invalidate();
                return false;
            }
        }
        if (options.isListenForDoubleClick()) {
            if (clickCount == 2) {
                long timeSinceFirstClick = System.nanoTime() - previousClickTimestamp;
                if (timeSinceFirstClick < options.getDoubleClickTimeLimitMs() * MS_TO_NS) {
                    listener.onDoubleClick();
                }
                invalidate();
            } else {
                long timePressed = System.nanoTime() - pressedTimestamp;
                long delay = options.getDoubleClickTimeLimitMs() + 1 - (timePressed / MS_TO_NS);
                if (options.isListenForClick()) {
                    handler.postDelayed(delayedClick, delay);
                } else {
                    handler.postDelayed(doubleClickTimeout, delay);
                }
            }
        } else if (options.isListenForClick()) {
            long timePressed = System.nanoTime() - pressedTimestamp;
            if (timePressed < options.getLongPressTimeMs() * MS_TO_NS) {
                listener.onClick();
            }
            invalidate();
        } else {
            invalidate();
        }
        return true;
    }

    private boolean onActionMove(MotionEvent event) {
        if (isEventValid && options.isIgnoreClickEventOnGestures()) {
            float x = event.getX(eventStartPointerIndex);
            float y = event.getY(eventStartPointerIndex);
            //We detect movement above threshold - its no longer a click but a translation.
            if (getDistance(startX, startY, x, y) > options.getTranslateStartThreshold()) {
                invalidate();
            }
            return true;
        }
        return false;
    }

    private boolean onActionPointerDown(MotionEvent event) {
        if (isEventValid && options.isIgnoreClickEventOnGestures()) {
            //Multitouch gesture started.
            invalidate();
            return true;
        }
        return false;
    }

    public void invalidate() {
        isEventValid = false;
        clickCount = 0;
        handler.removeCallbacks(delayedClick);
        handler.removeCallbacks(delayedLongPress);
        handler.removeCallbacks(doubleClickTimeout);
    }

    private static int getPointerIndex(MotionEvent event) {
        return event.getPointerId(event.getActionIndex());
    }

    private static double getDistance(float startX, float startY, float endX, float endY) {
        double a = endX - startX;
        double b = endY - startY;
        return Math.sqrt(a * a + b * b);
    }

    private boolean isListeningForSomething() {
        return options.isListenForClick() || options.isListenForDoubleClick() || options.isListenForLongClick();
    }
}
