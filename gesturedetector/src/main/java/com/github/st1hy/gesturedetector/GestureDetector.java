package com.github.st1hy.gesturedetector;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GestureDetector implements View.OnTouchListener, GestureListener {
    public boolean inDebug = Config.DEBUG;
    private static final String TAG = "GestureDetector";
    private final Context context;
    private final GestureListener listener;
    private final Options options;
    private final ClickDetector clickDetector;

    public GestureDetector(Context context, GestureListener listener) {
        if (context == null || listener == null) throw new NullPointerException();
        this.context = context;
        this.options = new Options(context);
        this.listener = listener;
        this.clickDetector = new ClickDetector(this, options);
    }

    public void invalidate() {
        clickDetector.invalidate();
    }

    public Options getOptions() {
        return options;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Action action = Action.valueOf(event.getAction());
//        if (inDebug) {
//            Log.d(TAG, event.toString());
//        }
        boolean isConsumed = clickDetector.onTouch(v, event);
        //TODO Add other detectors.
        return isConsumed;
    }

    @Override
    public void onScaleStarted() {
        listener.onScaleStarted();
    }

    @Override
    public void onScale(float scale) {
        listener.onScale(scale);
    }

    @Override
    public void onScaleEnded() {
        listener.onScaleEnded();
    }

    @Override
    public void onTranslateStarted() {
        listener.onTranslateStarted();
    }

    @Override
    public void onTranslate(float dx, float dy) {
        listener.onTranslate(dx, dy);
    }

    @Override
    public void onTranslateEnded() {
        listener.onTranslateEnded();
    }

    @Override
    public void onRotateStarted() {
        listener.onRotateStarted();
    }

    @Override
    public void onRotate(float rotation) {
        listener.onRotate(rotation);
    }

    @Override
    public void onRotateEnded() {
        listener.onRotateEnded();
    }

    @Override
    public void onClick() {
        listener.onClick();
        if (inDebug) {
            Log.d(TAG, "Clicked");
        }
    }

    @Override
    public void onLongPressed() {
        listener.onLongPressed();
        if (inDebug) {
            Log.d(TAG, "Long pressed");
        }
    }

    @Override
    public void onDoubleClick() {
        listener.onDoubleClick();
        if (inDebug) {
            Log.d(TAG, "Double clicked");
        }
    }

    private enum Action {
        DOWN(MotionEvent.ACTION_DOWN),
        UP(MotionEvent.ACTION_UP),
        MOVE(MotionEvent.ACTION_MOVE),
        ACTION_CANCEL(MotionEvent.ACTION_CANCEL),
        ACTION_OUTSIDE(MotionEvent.ACTION_OUTSIDE),
        ACTION_SCROLL(MotionEvent.ACTION_SCROLL),
        ACTION_POINTER_DOWN(MotionEvent.ACTION_POINTER_DOWN),
        ACTION_POINTER_UP(MotionEvent.ACTION_POINTER_UP),
        ;

        private final int internalCode;

        Action(int internalCode) {
            this.internalCode = internalCode;
        }

        public static Action valueOf(int motionEventAction) {
            for (Action action: values()) {
                if (action.internalCode == motionEventAction) return action;
            }
            return null;
        }
    }

}
