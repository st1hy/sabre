package com.github.st1hy.core;

import com.badlogic.gdx.math.Matrix3;

public interface Matrix3ChangedListener {

    void onMatrix3Changed(State state, Matrix3 matrix3);

    void onMatrix3Reset();

    enum State {
        STARTED, IN_PROGRESS, ENDED
    }
}
