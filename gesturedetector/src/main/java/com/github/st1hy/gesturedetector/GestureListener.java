package com.github.st1hy.gesturedetector;

import static com.github.st1hy.gesturedetector.Options.Constant;
import static com.github.st1hy.gesturedetector.Options.Event;

public interface GestureListener {
    /**
     * Is called when translation occurs. Require {@link Event#TRANSLATE} to be set.
     *
     * @param state    state of event. Can be either {@link State#STARTED} when {@link Constant#TRANSLATION_START_THRESHOLD} is first reached, {@link State#ENDED} when translation ends or {@link State#IN_PROGRESS}.
     * @param startX   position on x axis when finger was first pressed
     * @param startY   position on y axis when finger was first pressed
     * @param dx       initial translation on x axis when event was triggered
     * @param dy       initial translation on y axis when event was triggered
     * @param distance initial distance that triggered translation. Must be above {@link Constant#TRANSLATION_START_THRESHOLD}.
     */
    void onTranslate(State state, float startX, float startY, float dx, float dy, double distance);

    /**
     * Called when rotation is detected. Only received when {@link Event#TRANSLATE} is set.
     *
     * @param state    state of event. Can be either {@link State#STARTED} when {@link Constant#TRANSLATION_START_THRESHOLD} is first reached, {@link State#ENDED} when translation ends or {@link State#IN_PROGRESS}.
     * @param startX   position on x axis when finger was first pressed
     * @param startY   position on y axis when finger was first pressed
     * @param rotation rotation in degrees
     */
    void onRotate(State state, float startX, float startY, float rotation);

    void onScale(State state, float startX, float startY, float scale);

    void onClick(float startX, float startY);

    void onLongPressed(float startX, float startY);

    void onDoubleClick(float startX, float startY);

    void onFling(float startX, float startY, float velocity, FlingDirection direction);

    enum FlingDirection {
        UP, DOWN, LEFT, RIGHT
    }

    enum State {
        STARTED, IN_PROGRESS, ENDED
    }
}
