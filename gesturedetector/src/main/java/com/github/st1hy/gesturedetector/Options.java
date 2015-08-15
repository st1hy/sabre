package com.github.st1hy.gesturedetector;

import android.content.Context;

public final class Options implements Cloneable {
    private boolean listenForScale = true;
    private boolean listenForRotate = true;
    private boolean listenForTranslate = true;
    private boolean listenForClick = true;
    private boolean listenForLongClick = true;
    private boolean listenForDoubleClick = true;
    private boolean ignoreClickEventOnGestures = true;
    private int translateStartThreshold;
    private int longPressTimeMs = 500;
    private int doubleClickTimeLimitMs = 400;

    Options(Context context) {
        translateStartThreshold = context.getResources().getDimensionPixelSize(R.dimen.gesture_detector_translation_start_threshold);
    }

    public boolean isListenForDoubleClick() {
        return listenForDoubleClick;
    }

    public void setListenForDoubleClick(boolean listenForDoubleClick) {
        this.listenForDoubleClick = listenForDoubleClick;
    }

    public boolean isListenForClick() {
        return listenForClick;
    }

    public void setListenForClick(boolean listenForClick) {
        this.listenForClick = listenForClick;
    }

    public boolean isListenForTranslate() {
        return listenForTranslate;
    }

    public void setListenForTranslate(boolean listenForTranslate) {
        this.listenForTranslate = listenForTranslate;
    }

    public boolean isListenForRotate() {
        return listenForRotate;
    }

    public void setListenForRotate(boolean listenForRotate) {
        this.listenForRotate = listenForRotate;
    }

    public boolean isListenForScale() {
        return listenForScale;
    }

    public void setListenForScale(boolean listenForScale) {
        this.listenForScale = listenForScale;
    }

    public int getLongPressTimeMs() {
        return longPressTimeMs;
    }

    public void setLongPressTimeMs(int longPressTimeMs) {
        this.longPressTimeMs = longPressTimeMs;
    }

    public boolean isListenForLongClick() {
        return listenForLongClick;
    }

    public void setListenForLongClick(boolean listenForLongClick) {
        this.listenForLongClick = listenForLongClick;
    }

    public boolean isIgnoreClickEventOnGestures() {
        return ignoreClickEventOnGestures;
    }

    public void setIgnoreClickEventOnGestures(boolean ignoreClickEventOnGestures) {
        this.ignoreClickEventOnGestures = ignoreClickEventOnGestures;
    }

    public int getTranslateStartThreshold() {
        return translateStartThreshold;
    }

    public void setTranslateStartThreshold(int translateStartThreshold) {
        this.translateStartThreshold = translateStartThreshold;
    }

    public int getDoubleClickTimeLimitMs() {
        return doubleClickTimeLimitMs;
    }

    public void setDoubleClickTimeLimitMs(int doubleClickTimeLimitMs) {
        this.doubleClickTimeLimitMs = doubleClickTimeLimitMs;
    }

    public Options clone() {
        try {
            Object object = super.clone();
            if (!object.getClass().equals(getClass()))
                throw new IllegalStateException("Clone not the same class");
            return getClass().cast(object);
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
