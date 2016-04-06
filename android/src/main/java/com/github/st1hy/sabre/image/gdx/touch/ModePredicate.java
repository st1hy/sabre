package com.github.st1hy.sabre.image.gdx.touch;

import android.view.MotionEvent;
import android.view.View;

import com.badlogic.gdx.utils.Array;
import com.github.st1hy.sabre.libgdx.mode.UiMode;
import com.github.st1hy.sabre.libgdx.mode.UiModeChangeListener;
import com.github.st1hy.view.TouchPredicate;

public class ModePredicate implements TouchPredicate, UiModeChangeListener {
    private UiMode mode = UiMode.DEFAULT;
    private final Array<UiMode> expected;


    public ModePredicate(Array<UiMode> expected) {
        this.expected = expected;
    }

    @Override
    public boolean canForward(View v, MotionEvent event) {
        return expected.contains(mode, true);
    }

    @Override
    public void onUiModeChanged(UiMode newUiMode) {
        mode = newUiMode;
    }
}
