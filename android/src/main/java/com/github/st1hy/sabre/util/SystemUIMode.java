package com.github.st1hy.sabre.util;

import android.view.View;
import android.view.Window;

public enum SystemUIMode {
    DEFAULT(View.SYSTEM_UI_FLAG_VISIBLE),
    IMMERSIVE(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            | View.SYSTEM_UI_FLAG_IMMERSIVE),
    LOW_PROFILE(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LOW_PROFILE);
    private final int flags;

    SystemUIMode(int flags) {
        this.flags = flags;
    }

    public void apply(Window window) {
        window.getDecorView().setSystemUiVisibility(flags);
    }
}
