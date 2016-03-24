package com.github.st1hy.core.math;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class MeasuringRect {
    static Vector2 temp = new Vector2();
    public float left, right, top, bottom;

    public MeasuringRect(float left, float right, float bottom, float top) {
        if (right < left || top < bottom) throw new IllegalArgumentException("Right must be greater then left, top must be greater than bottom");
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public boolean contains(float x, float y) {
        return x >= left && x <= right && y >= bottom && y <= top;
    }

}
