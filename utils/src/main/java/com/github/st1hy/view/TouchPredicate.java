package com.github.st1hy.view;

import android.view.MotionEvent;
import android.view.View;

public interface TouchPredicate {
    boolean canForward(View v, MotionEvent event);
}
