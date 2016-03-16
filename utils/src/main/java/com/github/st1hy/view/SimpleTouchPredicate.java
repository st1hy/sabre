package com.github.st1hy.view;

import android.view.MotionEvent;
import android.view.View;

/**
 * Always return provided result.
 */
public class SimpleTouchPredicate implements TouchPredicate {
    private final boolean returnResult;

    public SimpleTouchPredicate(boolean returnResult) {
        this.returnResult = returnResult;
    }

    @Override
    public boolean canForward(View v, MotionEvent event) {
        return returnResult;
    }
}
