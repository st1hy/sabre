package com.github.st1hy.gesturedetector;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GestureDetector implements View.OnTouchListener {
    protected boolean inDebug = BuildConfig.DEBUG;
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
        this.clickDetector = new ClickDetector(listener, options);
    }

    public Options getOptions() {
        return options;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Action action = Action.valueOf(event.getAction());
        if (inDebug) {
            Log.d(TAG, event.toString());
        }
        boolean isConsumed = clickDetector.onTouch(v, event);
        //TODO
        return isConsumed;
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
