package com.github.st1hy.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class ImageScene implements ApplicationListener, Matrix3ChangedListener {
    private SpriteBatch batch;
    private final Matrix4 transformation = new Matrix4();
    private int startX,startY, imgWidthOut, imgHeightOut;
    private Texture texture;

    public ImageScene(Texture texture) {
        this.texture = texture;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
    }

    @Override
    public void onMatrix3Changed(Matrix3 matrix3) {
        transformation.set(matrix3);
        Gdx.graphics.requestRendering();
    }

    public void resetMatrix() {
        transformation.idt();
    }

    @Override
    public void resize(int width, int height) {
        int imgWidth = texture.getWidth();
        int imgHeight = texture.getHeight();
        float scale = Math.min((float) width / (float) imgWidth,
                (float) height / (float) imgHeight);
        startX = (int) ((width - imgWidth * scale) * 0.5f + 0.5f);
        startY = (int) ((height - imgHeight * scale) * 0.5f + 0.5f);
        imgWidthOut = (int) Math.ceil(imgWidth * scale);
        imgHeightOut = (int) Math.ceil(imgHeight * scale);
    }

    @Override
    public void render() {
        if (texture == null) return;
        batch.begin();
        batch.setTransformMatrix(transformation);
        batch.draw(texture, startX, startY, imgWidthOut, imgHeightOut);
        batch.end();
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        texture.dispose();
    }
}
