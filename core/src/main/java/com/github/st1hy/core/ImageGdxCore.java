package com.github.st1hy.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

public class ImageGdxCore implements ApplicationListener {
    private final Color background;
    private final SceneManager sceneManager;

    public ImageGdxCore(Color backgroundColor) {
        this.background = backgroundColor;
        sceneManager = new SceneManager();
    }

    public ImageGdxCore() {
        this(new Color());
    }

    public ImageScene setImage(Texture image) {
        ImageScene scene = new ImageScene(image);
        sceneManager.setCurrentScene(scene);
        return scene;
    }

    @Override
    public void create() {
        Gdx.graphics.setContinuousRendering(false);
        sceneManager.create();
    }

    @Override
    public void resize(int width, int height) {
        sceneManager.resize(width, height);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sceneManager.render();
    }

    @Override
    public void resume() {
        sceneManager.resume();
    }

    @Override
    public void pause() {
        sceneManager.pause();
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
    }
}
