package com.github.st1hy.core.math;

import com.badlogic.gdx.utils.FloatArray;

public class VerticesBoundsUtils {
    private VerticesBoundsUtils() {}

    public static FloatArray cropVertices(FloatArray inVertices, MeasuringRect bounds) {
        FloatArray outVertices = new FloatArray(inVertices.size);

        for (int i = 0; i < inVertices.size;) {
            float x = inVertices.items[i++];
            float y = inVertices.items[i++];
            if (x < bounds.left) x = bounds.left;
            else if (x > bounds.right) x = bounds.right;
            if (y < bounds.bottom) y = bounds.bottom;
            else if (y > bounds.top) y = bounds.top;
            outVertices.add(x);
            outVertices.add(y);
        }
        return outVertices;
    }
}
