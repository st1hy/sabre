package com.github.st1hy.sabre.image.gdx.touch;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.coregdx.OnPathChangedListener;
import com.github.st1hy.coregdx.TouchEventState;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.gesturedetector.TranslationDetector;

/**
 * Creates {@link FloatArray} from touch events.
 */
public class PathGestureDetector implements TranslationDetector.Listener {
    private final float minimalDistanceBetweenPoint;
    private OnPathChangedListener listener;
    private TouchEventState state = TouchEventState.ENDED;
    private int pathSize = 0;
    private float previousX, previousY, x, y;

    public PathGestureDetector(@NonNull Options options, @Nullable OnPathChangedListener listener) {
        this.listener = listener;
        minimalDistanceBetweenPoint = options.get(Options.Constant.TRANSLATION_START_THRESHOLD);
    }

    public PathGestureDetector(@NonNull Options options) {
        this(options, null);
    }

    public void setListener(@Nullable OnPathChangedListener listener) {
        this.listener = listener;
    }
    public void invalidate() {
        pathSize = 0;
    }

    @Override
    public void onTranslate(GestureEventState state, PointF startPoint, float x, float y, float _dx, float _dy, double _distance) {
        boolean newPointsAdded = state == GestureEventState.STARTED ||
                pathSize > 0 && movedMinimalDistance(x,y);
        if (newPointsAdded) {
            previousX = this.x;
            previousY = this.y;
            this.x = startPoint.x + x;
            this.y = startPoint.y + y;
            pathSize++;
        }
        if (state == GestureEventState.ENDED) {
            onTranslationEnded();
        } else if (newPointsAdded && pathSize >= 2) {
            switchToNextWorkingState();
            notifyAboutPath();
        }
    }

    private void switchToNextWorkingState() {
        switch (this.state) {
            case STARTED:
                this.state = TouchEventState.IN_PROGRESS;
                break;
            case ENDED:
                this.state = TouchEventState.STARTED;
                break;
        }
    }

    private boolean movedMinimalDistance(float x, float y) {
        float dx = x - previousX;
        float dy = y - previousY;
        double distance = distance(dx, dy);
        return distance > minimalDistanceBetweenPoint;
    }

    private void onTranslationEnded() {
        if (state != TouchEventState.ENDED) {
            state = TouchEventState.ENDED;
            notifyAboutPath();
        }
        pathSize = 0;
    }

    private void notifyAboutPath() {
        if (listener != null) listener.onPathChanged(state, x, y, previousX, previousY);
    }

    protected static double distance(float a, float b) {
        return Math.sqrt(a * a + b * b);
    }

}
