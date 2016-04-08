package com.github.st1hy.sabre.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.coregdx.OnPathChangedListener;
import com.github.st1hy.coregdx.TouchEventState;
import com.github.st1hy.sabre.libgdx.fragments.ImageFragmentCreator;

public class SelectionRenderer implements OnPathChangedListener {
    private final ScreenContext model;
    private final ImageFragmentCreator imageFragmentCreator;

    private final FloatArray polyLineArray = new FloatArray();
    private final Vector2 tempVector2 = new Vector2();
    private final Vector3 tempVector3 = new Vector3();

    public SelectionRenderer(ScreenContext model, ImageFragmentCreator imageFragmentCreator) {
        this.model = model;
        this.imageFragmentCreator = imageFragmentCreator;
    }

    public void render(ShapeRenderer renderer) {
        if (polyLineArray.size < 4) return;
        renderer.polyline(polyLineArray.items, 0, polyLineArray.size);
    }


    public void dispose() {
        polyLineArray.clear();
        polyLineArray.shrink();
    }

    @Override
    public void onPathChanged(TouchEventState state, float x, float y, float oldX, float oldY) {
        if (state == TouchEventState.STARTED) {
            polyLineArray.clear();
            polyLineArray.shrink();
            add(screenToWorldCoordinates(oldX, oldY));
        }
        add(screenToWorldCoordinates(x, y));
        if (state == TouchEventState.ENDED) {
            closePath();
        }
        Gdx.graphics.requestRendering();
    }

    private void add(Vector2 vec) {
        polyLineArray.add(vec.x);
        polyLineArray.add(vec.y);
    }

    private void closePath() {
        if (polyLineArray.size < 4) return;
        tempVector2.set(polyLineArray.get(0), polyLineArray.get(1));
        add(tempVector2);
        imageFragmentCreator.addNew(polyLineArray.toArray());
        polyLineArray.clear();
        polyLineArray.shrink();
    }

    private Vector2 screenToWorldCoordinates(float x, float y) {
        tempVector3.set(x, y, 0);
        tempVector3.mul(model.getWorldTransformation().getInvTransformation());
        tempVector2.set(tempVector3.x, tempVector3.y);
        return tempVector2;
    }

}
