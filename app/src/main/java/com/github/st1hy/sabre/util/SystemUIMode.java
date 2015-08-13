package com.github.st1hy.sabre.util;

import android.view.View;
import android.view.Window;

public enum SystemUIMode {
    DEFAULT {
        @Override
        public void apply(View decorView) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    },
    IMMERSIVE {
        @Override
        public void apply(View decorView) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);

        }
    },
    LOW_PROFILE {
        @Override
        void apply(View decorView) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }
    ;

    public void apply(Window window) {
        apply(window.getDecorView());
    }

    abstract void apply(View decorView);

}
