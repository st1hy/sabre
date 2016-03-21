package com.github.st1hy.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.core.State;

public class PathRenderer implements OnPathChangedListener {
    private float[] polyLine;

    public void render(ShapeRenderer renderer) {
        float[] polyLine = this.polyLine;
        if (polyLine == null || polyLine.length < 4) return;
        renderer.polyline(polyLine);
    }

    public void dispose() {
        polyLine = null;
    }

    @Override
    public void onPathChanged(State state, FloatArray polyLine) {
        this.polyLine = polyLine.toArray();
        Gdx.graphics.requestRendering();
    }
}
