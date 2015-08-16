package com.github.st1hy.gesturedetector;

import android.graphics.PointF;

import static com.github.st1hy.gesturedetector.Options.Constant;
import static com.github.st1hy.gesturedetector.Options.Event;

public interface GestureListener {
    /**
     * Is called when translation occurs. Require {@link Event#TRANSLATE} to be set in {@link Options}.
     *
     * @param state      state of event. Can be either {@link State#STARTED} when {@link Constant#TRANSLATION_START_THRESHOLD} is first reached, {@link State#ENDED} when translation ends or {@link State#IN_PROGRESS}.
     * @param startPoint position on the finger when it was first pressed
     * @param dx         initial translation on x axis when event was triggered
     * @param dy         initial translation on y axis when event was triggered
     * @param distance   distance between start point and current point that triggered translation. Must be above {@link Constant#TRANSLATION_START_THRESHOLD}.
     */
    void onTranslate(State state, PointF startPoint, float dx, float dy, double distance);

    /**
     * Called when rotation is detected. Only received when {@link Event#TRANSLATE} is set in {@link Options}.
     *
     * @param state       state of event. Can be either {@link State#STARTED} when {@link Constant#TRANSLATION_START_THRESHOLD} is first reached, {@link State#ENDED} when translation ends or {@link State#IN_PROGRESS}.
     * @param centerPoint point of reference for rotation
     * @param rotation    rotation in degrees since the beginning of the gesture
     */
    void onRotate(State state, PointF centerPoint, float rotation);

    /**
     * Called when scaling is detected. Only received when {@link Event#SCALE} is set in {@link Options}.
     *
     * @param state       state of event. Can be either {@link State#STARTED} when {@link Constant#TRANSLATION_START_THRESHOLD} is first reached, {@link State#ENDED} when translation ends or {@link State#IN_PROGRESS}.
     * @param centerPoint center of the gesture from the moment of event start.
     * @param scale       how much distance between points have grown since the beginning of this gesture
     */
    void onScale(State state, PointF centerPoint, float scale);

    void onClick(PointF startPoint);

    void onLongPressed(PointF startPoint);

    void onDoubleClick(PointF startPoint);

    void onFling(PointF startPoint, float velocity, FlingDirection direction);

    enum FlingDirection {
        UP, DOWN, LEFT, RIGHT
    }

    enum State {
        STARTED, IN_PROGRESS, ENDED
    }
}
