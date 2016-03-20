package com.github.st1hy.sabre.image.gdx.touch;

import android.view.MotionEvent;
import android.view.View;

import com.github.st1hy.core.mode.UiMode;
import com.github.st1hy.core.mode.UiModeChangeListener;
import com.github.st1hy.view.TouchPredicate;

public class EditModePredicate implements TouchPredicate, UiModeChangeListener {
    private UiMode mode = UiMode.DEFAULT;

    @Override
    public boolean canForward(View v, MotionEvent event) {
        return mode == UiMode.CUT_ELEMENT;
    }

    @Override
    public void onUiModeChanged(UiMode newUiMode) {
        mode = newUiMode;
    }
}
