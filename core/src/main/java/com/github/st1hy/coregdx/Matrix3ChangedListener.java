package com.github.st1hy.coregdx;

import com.badlogic.gdx.math.Matrix3;

public interface Matrix3ChangedListener {

    void onMatrix3Changed(TouchEventState state, Matrix3 matrix3);

    void onMatrix3Reset();

}
