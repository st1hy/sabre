package com.github.st1hy.sabre.image.gdx.touch;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.github.st1hy.gesturedetector.FlingDetector;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.MultipleGestureListener;
import com.github.st1hy.gesturedetector.Options;

import java.util.EnumSet;

import static com.github.st1hy.gesturedetector.Options.Event.CLICK;
import static com.github.st1hy.gesturedetector.Options.Event.DOUBLE_CLICK;
import static com.github.st1hy.gesturedetector.Options.Event.FLING;
import static com.github.st1hy.gesturedetector.Options.Event.LONG_PRESS;
import static com.github.st1hy.gesturedetector.Options.Event.MATRIX_TRANSFORMATION;
import static com.github.st1hy.gesturedetector.Options.Event.ROTATE;
import static com.github.st1hy.gesturedetector.Options.Event.SCALE;
import static com.github.st1hy.gesturedetector.Options.Event.TRANSLATE;

public class SelectiveGestureListenerDelegate implements MultipleGestureListener {
    private final MultipleGestureListener delegate;
    private final EnumSet<Options.Event> allowed;

    public SelectiveGestureListenerDelegate(@NonNull MultipleGestureListener delegate, @NonNull EnumSet<Options.Event> allowed) {
        this.delegate = delegate;
        this.allowed = allowed;
    }

    @Override
    public void onMatrix(GestureEventState state, Matrix currentTransformation) {
        if (isAllowed(MATRIX_TRANSFORMATION)) delegate.onMatrix(state, currentTransformation);
    }

    @Override
    public void onTranslate(GestureEventState state, PointF startPoint, float x, float y, float dx, float dy, double distance) {
        if (isAllowed(TRANSLATE)) delegate.onTranslate(state, startPoint, x, y, dx, dy, distance);
    }

    @Override
    public void onRotate(GestureEventState state, PointF centerPoint, double rotation, double delta) {
        if (isAllowed(ROTATE)) delegate.onRotate(state, centerPoint, rotation, delta);
    }

    @Override
    public void onScale(GestureEventState state, PointF centerPoint, float scale, float scaleRelative) {
        if (isAllowed(SCALE)) delegate.onScale(state, centerPoint, scale, scaleRelative);
    }

    @Override
    public void onClick(PointF startPoint) {
        if (isAllowed(CLICK)) delegate.onClick(startPoint);
    }

    @Override
    public void onLongPressed(PointF startPoint) {
        if (isAllowed(LONG_PRESS)) delegate.onLongPressed(startPoint);
    }

    @Override
    public void onDoubleClick(PointF startPoint) {
        if (isAllowed(DOUBLE_CLICK)) delegate.onDoubleClick(startPoint);
    }

    @Override
    public void onFling(PointF startPoint, float velocity, FlingDetector.Direction direction) {
        if (isAllowed(FLING)) delegate.onFling(startPoint, velocity, direction);
    }
    
    private boolean isAllowed(Options.Event event) {
        return allowed.contains(event);
    }
}
