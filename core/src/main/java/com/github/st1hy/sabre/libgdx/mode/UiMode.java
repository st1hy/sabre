package com.github.st1hy.sabre.libgdx.mode;

import com.github.st1hy.utils.EventBus;
import com.github.st1hy.utils.EventMethod;

public enum UiMode {
    MOVE_CAMERA, MOVE_ELEMENT, CUT_ELEMENT;

    public static final UiMode DEFAULT = MOVE_CAMERA;
    public static UiMode GLOBAL = DEFAULT;

    public static void setGlobalMode(final UiMode mode) {
        if (GLOBAL == mode) return;
        GLOBAL = mode;
        EventBus.INSTANCE.apply(UiModeChangeListener.class, new EventMethod<UiModeChangeListener>() {
            @Override
            public void apply(UiModeChangeListener uiModeChangeListener) {
                uiModeChangeListener.onUiModeChanged(mode);
            }
        });
    }

    public static void registerChangeListener(UiModeChangeListener listener) {
        EventBus.INSTANCE.add(UiModeChangeListener.class, listener);
    }

    public static void unregisterChangeListener(UiModeChangeListener listener) {
        EventBus.INSTANCE.remove(UiModeChangeListener.class, listener);
    }
}
