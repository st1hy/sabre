package com.github.st1hy.gesturedetector;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Listens for gesture events.
 * <p>
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

    protected GestureDetectorImp(Context context, GestureListener listener, Options options) {
        this.context = context;
        this.options = options; new Options(context);
        this.listener = listener;
        this.clickDetector = new ClickDetector(this, options);
        this.translationDetector = new TranslationDetector(this, options);
    }

    /**
     * Create new gesture detector.
     * @param context used to provide resources and display metrics.
     * @param listener will receive calls about gestures.
     * @param options describes detector parameters. Can be null.
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
        isConsumed |=  translationDetector.onTouch(v, event);
        //TODO Add other detectors.
        return isConsumed;
    }

    @Override
    public void onTranslate(State state, float startX, float startY, float dx, float dy, double distance) {
        clickDetector.onTranslate(state, startX, startY, dx, dy, distance);
        listener.onTranslate(state, startX, startY, dx, dy, distance);
        if (inDebug) {
            Log.d(TAG, String.format("Translation %s: startX = %.1f, startY = %.1f, dx = %.1f, dy = %.1f, distance = %.2f", state.toString(), startX, startY, dx, dy, distance));
        }
    }

    @Override
    public void onRotate(State state, float startX, float startY, float rotation) {
        listener.onRotate(state, startX, startY, rotation);
    }

    @Override
    public void onScale(State state, float startX, float startY, float scale) {
        listener.onScale(state, startX, startY, scale);
    }

    @Override
    public void onClick(float startX, float startY) {
        listener.onClick(startX, startY);
        if (inDebug) {
            Log.d(TAG, String.format("Clicked: x = %.1f, y = %.1f", startX, startY));
        }
    }

    @Override
    public void onLongPressed(float startX, float startY) {
        listener.onLongPressed(startX, startY);
        if (inDebug) {
            Log.d(TAG, String.format("Long pressed: x = %.1f, y = %.1f", startX, startY));
        }
    }

    @Override
    public void onDoubleClick(float startX, float startY) {
        listener.onDoubleClick(startX, startY);
        if (inDebug) {
            Log.d(TAG, String.format("Double clicked: x = %.1f, y = %.1f", startX, startY));
        }
    }

    @Override
    public void onFling(float startX, float startY, float velocity, FlingDirection direction) {
        listener.onFling(startX, startY, velocity, direction);
    }
}
