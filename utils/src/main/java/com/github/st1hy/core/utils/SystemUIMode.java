package com.github.st1hy.core.utils;

import android.annotation.TargetApi;
import android.view.View;
import android.view.Window;

public enum SystemUIMode {
    DEFAULT(View.SYSTEM_UI_FLAG_VISIBLE),
    IMMERSIVE(getImmersiveFlags()),
    IMMERSIVE_STICKY(getImmersiveStickyFlags()),
    LAYOUT_FULLSCREEN(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN),
    LOW_PROFILE(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LOW_PROFILE);

    private final int flags;

    SystemUIMode(int flags) {
        this.flags = flags;
    }

    public void apply(Window window) {
        window.getDecorView().setSystemUiVisibility(flags);
    }


    private static int getImmersiveFlags() {
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
        if (Utils.hasKitKat()) {
            flags |= getImmersiveFlagsApi19();
        }
        return flags;
    }

    @TargetApi(19)
    private static int getImmersiveFlagsApi19() {
        return View.SYSTEM_UI_FLAG_IMMERSIVE;
    }

    private static int getImmersiveStickyFlags() {
        int flags = getImmersiveFlags();
        if (Utils.hasKitKat()) {
            flags |= getImmersiveStickyFlagsApi19();
        }
        return flags;
    }

    @TargetApi(19)
    private static int getImmersiveStickyFlagsApi19() {
        return View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }
}
