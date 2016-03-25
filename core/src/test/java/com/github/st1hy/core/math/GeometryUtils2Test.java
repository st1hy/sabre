package com.github.st1hy.core.math;

import com.badlogic.gdx.math.Rectangle;

import org.junit.Assert;
import org.junit.Test;

public class GeometryUtils2Test {

    @Test
    public void testIntersection() {
        Rectangle r1 = new Rectangle(0, 0, 4, 4);
        Rectangle r2 = new Rectangle(1, 1, 1, 1);
        Rectangle expected = new Rectangle(1, 1, 1, 1);
        Assert.assertEquals(expected, GeometryUtils2.intersect(r1, r2));
        Assert.assertEquals(expected, GeometryUtils2.intersect(r2, r1));

        r2 = new Rectangle(-1, -1, 1, 1);
        expected = new Rectangle(0, 0, 0, 0);
        Assert.assertEquals(expected, GeometryUtils2.intersect(r1, r2));
        Assert.assertEquals(expected, GeometryUtils2.intersect(r2, r1));

        r2 = new Rectangle(-2, -2, 1, 1);
        expected = new Rectangle(0, 0, -1, -1);
        Assert.assertEquals(expected, GeometryUtils2.intersect(r1, r2));
        Assert.assertEquals(expected, GeometryUtils2.intersect(r2, r1));

    }
}