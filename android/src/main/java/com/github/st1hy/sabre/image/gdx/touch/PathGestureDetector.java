package com.github.st1hy.sabre.image.gdx.touch;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.core.State;
import com.github.st1hy.core.screen.path.OnPathChangedListener;
import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.gesturedetector.TranslationDetector;

/**
 * Creates {@link FloatArray} from touch events.
 */
public class PathGestureDetector implements GestureDetector, TranslationDetector.Listener {
    private final TranslationDetector translationDetector;
    private final float minimalDistanceBetweenPoint;
    private OnPathChangedListener listener;
    private State state = State.ENDED;
    private boolean revertY = false;
    private int height;
    private int pathSize = 0;
    private float previousX, previousY, x, y;

    public PathGestureDetector(@NonNull Context context, @Nullable OnPathChangedListener listener) {
        this.listener = listener;
        Options options = new Options(context.getResources());
        options.setFlag(Options.Flag.TRANSLATION_STRICT_ONE_FINGER, true);
        minimalDistanceBetweenPoint = options.get(Options.Constant.TRANSLATION_START_THRESHOLD);
        this.translationDetector = new TranslationDetector(this, options);
    }

    public PathGestureDetector(@NonNull Context context) {
        this(context, null);
    }

    public void setListener(@Nullable OnPathChangedListener listener) {
        this.listener = listener;
    }

    public PathGestureDetector setRevertY(boolean revertY) {
        this.revertY = revertY;
        return this;
    }

    @Override
    public void invalidate() {
        translationDetector.invalidate();
        pathSize = 0;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) height = v.getHeight();
        return translationDetector.onTouch(v, event);
    }

    @Override
    public void onTranslate(GestureEventState state, PointF startPoint, float x, float y, float _dx, float _dy, double _distance) {
        boolean newPointsAdded = state == GestureEventState.STARTED ||
                pathSize > 0 && movedMinimalDistance(x,y);
        if (newPointsAdded) {
            previousX = this.x;
            previousY = this.y;
            this.x = startPoint.x + x;
            float realY = startPoint.y + y;
            if (revertY) realY = height - realY;
            this.y = realY;
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
                this.state = State.IN_PROGRESS;
                break;
            case ENDED:
                this.state = State.STARTED;
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
        if (state != State.ENDED) {
            state = State.ENDED;
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
