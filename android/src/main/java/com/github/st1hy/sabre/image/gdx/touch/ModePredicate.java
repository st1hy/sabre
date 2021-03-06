package com.github.st1hy.sabre.image.gdx.touch;

import android.view.MotionEvent;
import android.view.View;

import com.github.st1hy.sabre.libgdx.mode.UiMode;
import com.github.st1hy.view.TouchPredicate;

public class ModePredicate implements TouchPredicate {
    private UiMode mode = UiMode.DEFAULT;
    private final UiMode expected;


    public ModePredicate(UiMode expected) {
        this.expected = expected;
    }

    @Override
    public boolean canForward(View v, MotionEvent event) {
        return expected == mode;
    }

    public void onUiModeChanged(UiMode newUiMode) {
        mode = newUiMode;
    }
}
