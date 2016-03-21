package com.github.st1hy.core.screen;

import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.core.State;

public interface OnPathChangedListener {
    /**
     *
     * @param state show state of drawing line.
     * @param polyLine array containing chained float points x1, y1, x2, y2, etc. (Min size 4, dividable by 2)
     */
    void onPathChanged(State state, FloatArray polyLine);
}
