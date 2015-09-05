package com.github.st1hy.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

public class GestureListenerImp implements GestureDetector.GestureListener {
    private static final String TAG = "GestureListenerImp";
    private final ImageGdxCore imageGdxCore;
    public GestureListenerImp(ImageGdxCore imageGdxCore) {
        this.imageGdxCore = imageGdxCore;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        imageGdxCore.getTransformation().translate(deltaX, -deltaY, 0);
        Gdx.app.log(TAG, imageGdxCore.getTransformation().toString());
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
//        float scale = distance / initialDistance;
//        imageGdxCore.transformation.(scale, -scale, 1);
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
