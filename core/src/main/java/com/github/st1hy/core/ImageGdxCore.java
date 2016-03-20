package com.github.st1hy.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.github.st1hy.core.screen.ImageScreen;
import com.github.st1hy.core.screen.ScreenManager;

public class ImageGdxCore implements ApplicationListener {
    private final Color background;
    private final ScreenManager screenManager;
    private final TextureManager textureManager;

    public ImageGdxCore(Color backgroundColor) {
        this.background = backgroundColor;
        screenManager = new ScreenManager();
        textureManager = new TextureManager();
    }

    public ImageGdxCore() {
        this(new Color());
    }

    public ImageScreen setImage(Texture image) {
        ImageScreen screen = new ImageScreen(image);
        screenManager.setCurrentScreen(screen);
        return screen;
    }

    @Override
    public void create() {
        Gdx.graphics.setContinuousRendering(false);
        screenManager.create();
    }

    @Override
    public void resize(int width, int height) {
        screenManager.resize(width, height);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        screenManager.render();
    }

    @Override
    public void resume() {
        screenManager.resume();
    }

    @Override
    public void pause() {
        screenManager.pause();
    }

    @Override
    public void dispose() {
        screenManager.dispose();
        textureManager.dispose();
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }
}
