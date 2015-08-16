package com.github.st1hy.gesturedetector;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Listens for gesture events.
 * <p/>
 * Events are delivered through {@link GestureListener}. {@link Options} control which events will be triggered and various thresholds and settings.
 */
public class GestureDetectorImp implements GestureListener, GestureDetector {
    private static final boolean inDebug = Config.DEBUG;
    private static final String TAG = "GestureDetector";
    private final Context context;
    private final GestureListener listener;
    private final Options options;
    private final ClickDetector clickDetector;
    private final TranslationDetector translationDetector;
    private final ScaleDetector scaleDetector;

    protected GestureDetectorImp(Context context, GestureListener listener, Options options) {
        this.context = context;
        this.options = options;
        this.listener = listener;
        this.clickDetector = new ClickDetector(this, options);
        this.translationDetector = new TranslationDetector(this, options);
        this.scaleDetector = new ScaleDetector(this, options);
    }

    /**
     * Create new gesture detector.
     *
     * @param context  used to provide resources and display metrics.
     * @param listener will receive calls about gestures.
     * @param options  describes detector parameters. Can be null.
     * @return New gesture detector.
     * @throws NullPointerException when context or listener are null.
     */
    public static GestureDetector newInstance(Context context, GestureListener listener, Options options) {
        if (context == null || listener == null) throw new NullPointerException();
        if (options == null) options = new Options(context);
        return new GestureDetectorImp(context, listener, options);
    }

    @Override
    public void invalidate() {
        clickDetector.invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        if (inDebug) {
//            Log.d(TAG, event.toString());
//        }
        boolean isConsumed = clickDetector.onTouch(v, event);
        isConsumed |= translationDetector.onTouch(v, event);
        isConsumed |= scaleDetector.onTouch(v, event);
        //TODO Add other detectors.
        return isConsumed;
    }

    @Override
    public void onTranslate(State state, PointF startPoint, float dx, float dy, double distance) {
        clickDetector.onTranslate(state, startPoint, dx, dy, distance);
        listener.onTranslate(state, startPoint, dx, dy, distance);
        if (inDebug) {
            Log.d(TAG, String.format("Translation %s: startX = %.1f, startY = %.1f, dx = %.1f, dy = %.1f, distance = %.2f", state.toString(), startPoint.x, startPoint.y, dx, dy, distance));
        }
    }

    @Override
    public void onRotate(State state, PointF centerPoint, float rotation) {
        listener.onRotate(state, centerPoint, rotation);
    }

    @Override
    public void onScale(State state, PointF centerPoint, float scale) {
        listener.onScale(state, centerPoint, scale);
        if (inDebug) {
            Log.d(TAG, String.format("Scale %s: x = %.1f, y = %.1f, scale = %.2f",state.toString(), centerPoint.x, centerPoint.y, scale));
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
    public void onFling(PointF startPoint, float velocity, FlingDirection direction) {
        listener.onFling(startPoint, velocity, direction);
    }
}
