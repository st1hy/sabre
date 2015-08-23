package com.github.st1hy.gesturedetector;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

/**
 * Listens for gesture events.
 * <p/>
 * Events are delivered through {@link GestureListener}. {@link Options} control which events will be triggered and various thresholds and settings.
 */
public class GestureDetectorImp implements GestureListener, GestureDetector {
    protected static final boolean inDebug = Config.DEBUG;
    protected static final String TAG = "GestureDetector";
    protected final GestureListener listener;
    protected final ClickDetector clickDetector;
    protected final List<GestureDetector> detectors;

    protected GestureDetectorImp(GestureListener listener, Options options) {
        this.listener = listener;
        this.detectors = new LinkedList<>();
        this.clickDetector = new ClickDetector(this, options);
        this.detectors.add(clickDetector);
        if (options.isEnabled(Options.Event.TRANSLATE)) this.detectors.add(new TranslationDetector(this, options));
        if (options.isEnabled(Options.Event.SCALE)) this.detectors.add(new ScaleDetector(this, options));
        if (options.isEnabled(Options.Event.ROTATE)) this.detectors.add(new RotationDetector(this, options));
        if (options.isEnabled(Options.Event.FLING)) this.detectors.add(new FlingDetector(this, options));
    }

    /**
     * Create new gesture detector.
     *
     * @param listener will receive calls about gestures.
     * @param options  describes detector parameters.
     * @return New gesture detector.
     * @throws NullPointerException when listener or options are null.
     */
    public static GestureDetector newInstance(GestureListener listener, Options options) {
        if (listener == null || options == null) throw new NullPointerException();
        return new GestureDetectorImp(listener, options);
    }

    @Override
    public void invalidate() {
        for (GestureDetector detector: detectors) {
            detector.invalidate();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        if (inDebug) {
//            Log.d(TAG, event.toString());
//        }
        boolean isConsumed = false;
        for (GestureDetector detector: detectors) {
            isConsumed |= detector.onTouch(v, event);
        }
        return isConsumed;
    }

    @Override
    public void onTranslate(GestureEventState state, PointF startPoint, float dx, float dy, double distance) {
        clickDetector.onTranslate(state, startPoint, dx, dy, distance);
        listener.onTranslate(state, startPoint, dx, dy, distance);
        if (inDebug) {
            Log.d(TAG, String.format("Translation %s: startX = %.1f, startY = %.1f, dx = %.1f, dy = %.1f, distance = %.2f", state.toString(), startPoint.x, startPoint.y, dx, dy, distance));
        }
    }

    @Override
    public void onRotate(GestureEventState state, PointF centerPoint, double rotation, double delta) {

        listener.onRotate(state, centerPoint, rotation, delta);
        if (inDebug) {
            Log.d(TAG, String.format("Rotation %s: x = %.1f, y = %.1f, rotation = %.2f", state.toString(), centerPoint.x, centerPoint.y, rotation));
        }
    }

    @Override
    public void onScale(GestureEventState state, PointF centerPoint, float scale) {
        listener.onScale(state, centerPoint, scale);
        if (inDebug) {
            Log.d(TAG, String.format("Scale %s: x = %.1f, y = %.1f, scale = %.2f", state.toString(), centerPoint.x, centerPoint.y, scale));
        }
    }

    @Override
    public void onClick(PointF startPoint) {
        listener.onClick(startPoint);
        if (inDebug) {
            Log.d(TAG, String.format("Clicked: x = %.1f, y = %.1f", startPoint.x, startPoint.y));
        }
    }

    @Override
    public void onLongPressed(PointF startPoint) {
        listener.onLongPressed(startPoint);
        if (inDebug) {
            Log.d(TAG, String.format("Long pressed: x = %.1f, y = %.1f", startPoint.x, startPoint.y));
        }
    }

    @Override
    public void onDoubleClick(PointF startPoint) {
        listener.onDoubleClick(startPoint);
        if (inDebug) {
            Log.d(TAG, String.format("Double clicked: x = %.1f, y = %.1f", startPoint.x, startPoint.y));
        }
    }

    @Override
    public void onFling(PointF startPoint, float velocity, FlingDetector.Direction direction) {
        listener.onFling(startPoint, velocity, direction);
        if (inDebug) {
            Log.d(TAG, String.format("Fling %s: x = %.1f, y = %.1f, velocity = %.2f", direction.toString(), startPoint.x, startPoint.y, velocity));
        }
    }
}
