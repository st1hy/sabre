package com.github.st1hy.core.screen;

import com.github.st1hy.core.State;

public interface OnPathChangedListener {
    /**
     *
     * @param state show state of drawing line.
     * @param x x coordinate of new point in path
     * @param y y coordinate of new point in path
     * @param oldX x coordinate of previous point in path
     * @param oldY y coordinate of previous point in path
     */
    void onPathChanged(State state, float x, float y, float oldX, float oldY);
}
