package com.github.st1hy.sabre.image.gdx.touch;

import android.view.MotionEvent;
import android.view.View;

import com.github.st1hy.view.ForwardingOnTouchListener;

public class DeadZoneTouchPredicate implements ForwardingOnTouchListener.TouchPredicate {
    private static final float deadZoneSize = 0.01f;

    @Override
    public boolean canForward(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return onPointerDown(v, event);
            default:
                return true;
        }
    }

    private boolean onPointerDown(View v, MotionEvent event) {
        int height = v.getHeight();
        float y = event.getY();
        float deadZone = height * deadZoneSize;
        return y > deadZone && y < height - deadZone;
    }
}
