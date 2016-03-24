package com.github.st1hy.core.screen.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.core.State;
import com.github.st1hy.core.screen.fragments.ImageFragmentCreator;

public class PathRenderer implements OnPathChangedListener {
    private final FloatArray polyLineArray = new FloatArray();
    private final Matrix4 transformation = new Matrix4(), invTransformation = new Matrix4();
    private boolean matrixDirty = true;
    private final Vector2 tempVector2 = new Vector2();
    private final Vector3 tempVector3 = new Vector3();
    private final ImageFragmentCreator imageFragmentCreator;

    public PathRenderer(ImageFragmentCreator imageFragmentCreator) {
        this.imageFragmentCreator = imageFragmentCreator;
    }

    public void setTransformation(Matrix4 transformation) {
        this.transformation.set(transformation);
        matrixDirty = true;
    }

    public void render(ShapeRenderer renderer) {
        if (polyLineArray.size < 4) return;
        renderer.polyline(polyLineArray.items, 0, polyLineArray.size);
    }

    private void setupMatrix() {
        invTransformation.set(transformation).inv();
    }

    public void dispose() {
        polyLineArray.clear();
        polyLineArray.shrink();
    }

    @Override
    public void onPathChanged(State state, float x, float y, float oldX, float oldY) {
        if (matrixDirty) {
            matrixDirty = false;
            setupMatrix();
        }
        if (state == State.STARTED) {
            polyLineArray.clear();
            polyLineArray.shrink();
            add(screenToWorldCoordinates(oldX, oldY));
        }
        add(screenToWorldCoordinates(x, y));
        if (state == State.ENDED) {
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
        imageFragmentCreator.addNew(polyLineArray);
//        EventBus.INSTANCE.apply(UiModeChangeListener.class, setObjectMode);
        polyLineArray.clear();
        polyLineArray.shrink();
    }

    private Vector2 screenToWorldCoordinates(float x, float y) {
        tempVector3.set(x, y, 0);
        tempVector3.mul(invTransformation);
        tempVector2.set(tempVector3.x, tempVector3.y);
        return tempVector2;
    }

//    private final EventMethod<UiModeChangeListener> setObjectMode = new EventMethod<UiModeChangeListener>() {
//        @Override
//        public void apply(UiModeChangeListener uiModeChangeListener) {
//            if (uiModeChangeListener != null) uiModeChangeListener.onUiModeChanged(UiMode.MOVE_ELEMENT);
//        }
//    };

}
