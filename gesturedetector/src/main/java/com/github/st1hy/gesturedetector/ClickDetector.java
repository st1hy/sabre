package com.github.st1hy.gesturedetector;

import android.view.MotionEvent;
import android.view.View;

/**
 * Listens for click events.
 * Calls {@link GestureListener#onClick()}, {@link GestureListener#onLongPressed()} ()} or {@link GestureListener#onDoubleClick()} when appropriate.
 *
 * If option {@link Options#ignoreClickEventOnGestures} is set it will filter out clicks that are part of more complicated gestures.
 *
 * When listening also for double click events, adds delay for second press event to occur.
 * If it doesn't it triggers {@link GestureListener#onClick()}
 */
class ClickDetector implements View.OnTouchListener {
    private final GestureListener listener;
    private Options options;
    private long pressedTimestamp;

    ClickDetector(GestureListener listener, Options options) {
        this.listener = listener;
        this.options = options;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!options.isListenForClick()) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressedTimestamp = System.nanoTime();
        }
        return true;
    }
}
