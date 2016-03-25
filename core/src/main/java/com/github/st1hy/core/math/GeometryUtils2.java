package com.github.st1hy.core.math;

import com.badlogic.gdx.math.Rectangle;

public class GeometryUtils2 {

    /**
     * Creates intersection from 2 rectangles.
     * If intersection does not exist width or height of resulting rectangle is <= 0.
     */
    public static Rectangle intersect(Rectangle r1, Rectangle r2) {
        float x = Math.max(r1.x, r2.x);
        float y = Math.max(r1.y, r2.y);
        float width = Math.min(r1.x + r1.width, r2.x + r2.width) - x;
        float height = Math.min(r1.y + r1.height, r2.y + r2.height) - y;
        return new Rectangle(x, y, width, height);
    }
}
