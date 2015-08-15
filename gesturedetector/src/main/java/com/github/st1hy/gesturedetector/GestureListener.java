package com.github.st1hy.gesturedetector;

public interface GestureListener {
    void onScaleStarted();
    void onScale(float scale);
    void onScaleEnded();
    void onTranslateStarted();
    void onTranslate(float dx, float dy);
    void onTranslateEnded();
    void onRotateStarted();
    void onRotate(float rotation);
    void onRotateEnded();
    void onClick();
    void onLongPressed();
    void onDoubleClick();
}
