package com.github.st1hy.sabre.image.gdx.touch;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.core.State;
import com.github.st1hy.core.screen.OnPathChangedListener;
import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.gesturedetector.TranslationDetector;
import com.github.st1hy.sabre.BuildConfig;

/**
 * Creates {@link FloatArray} from touch events.
 */
public class PathGestureDetector implements GestureDetector, TranslationDetector.Listener {
    private final TranslationDetector translationDetector;
    private final FloatArray points = new FloatArray();
    private final float minimalDistanceBetweenPoint;
    private OnPathChangedListener listener;
    private State state = State.ENDED;
    private boolean revertY = false;
    private int height;

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
        points.clear();
        points.shrink();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) height = v.getHeight();
        return translationDetector.onTouch(v, event);
    }

    @Override
    public void onTranslate(GestureEventState state, PointF startPoint, float x, float y, float _dx, float _dy, double _distance) {
        boolean newPointsAdded = state == GestureEventState.STARTED ||
                points.size > 0 && movedMinimalDistance(x,y);
        if (newPointsAdded) {
            points.add(startPoint.x + x);
            float realY = startPoint.y + y;
            if (revertY) realY = height - realY;
            points.add(realY);
        }
        if (state == GestureEventState.ENDED) {
            onTranslationEnded();
        } else if (newPointsAdded && points.size >= 4) {
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
        int size = points.size;
        float lastX = points.get(size - 2);
        float lastY = points.get(size - 1);
        float dx = x - lastX;
        float dy = y - lastY;
        double distance = distance(dx, dy);
        return distance > minimalDistanceBetweenPoint;
    }

    private void onTranslationEnded() {
        if (state != State.ENDED) {
            state = State.ENDED;
            notifyAboutPath();
        }
        points.clear();
        points.shrink();
    }

    private void notifyAboutPath() {
        if (BuildConfig.DEBUG) {
//            Timber.d("Path %s: %s", state.name(), points.toString());
        }
        if (listener != null) listener.onPathChanged(state, points);
    }

    protected static double distance(float a, float b) {
        return Math.sqrt(a * a + b * b);
    }

}
