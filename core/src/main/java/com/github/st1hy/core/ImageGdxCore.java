package com.github.st1hy.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Matrix4;

public class ImageGdxCore extends ApplicationAdapter {
    private SpriteBatch batch;
    private int windowWidth, windowHeight;
    private final Matrix4 transformation = new Matrix4();
    private int startX,startY, imgWidthOut, imgHeightOut;
    private final GestureDetector.GestureListener inputProcessor = new GestureListenerImp(this);
    private ImageTexture texture;

    @Override
    public void create() {
        batch = new SpriteBatch();
//        texture = new ImageTexture(new Texture("badlogic.jpg"));
        Gdx.graphics.setContinuousRendering(false);
        Gdx.input.setInputProcessor(new GestureDetector(inputProcessor));
    }

    public void loadTexture(ImageTexture texture) {
        this.texture = texture;
        configureBounds();
    }

    public ImageTexture getTexture() {
        return texture;
    }

    public Matrix4 getTransformation() {
        return transformation;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.windowWidth = width;
        this.windowHeight = height;
        configureBounds();
    }

    private void configureBounds() {
        if (texture == null) return;
        Texture img = texture.getTexture();
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        float scale = Math.min((float) windowWidth / (float) imgWidth,
                (float) windowHeight / (float) imgHeight);
        startX = (int) ((windowWidth - imgWidth * scale) * 0.5f + 0.5f);
        startY = (int) ((windowHeight - imgHeight * scale) * 0.5f + 0.5f);
        imgWidthOut = (int) Math.ceil(imgWidth * scale);
        imgHeightOut = (int) Math.ceil(imgHeight * scale);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (texture == null) return;
        batch.begin();
        batch.setTransformMatrix(transformation);
        batch.draw(texture.getTexture(), startX, startY, imgWidthOut, imgHeightOut);
        batch.end();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void pause() {
        super.pause();
    }
}
