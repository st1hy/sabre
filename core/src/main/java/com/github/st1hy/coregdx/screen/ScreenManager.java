package com.github.st1hy.coregdx.screen;

public class ScreenManager implements Screen {
    private static final int UNKNOWN_SIZE = -1;
    private int width = UNKNOWN_SIZE, height = UNKNOWN_SIZE;
    private Screen currentScene;
    private State state = State.DISPOSED;

    public void setCurrentScreen(Screen newScene) {
        if (currentScene != null) {
            if (state != State.DISPOSED) {
                if (state == State.RESUMED) currentScene.pause();
                currentScene.dispose();
            }
        }
        this.currentScene = newScene;
        if (newScene != null) {
            if (state != State.DISPOSED) {
                newScene.create();
                if (width != UNKNOWN_SIZE && height != UNKNOWN_SIZE) newScene.resize(width, height);
                if (state == State.RESUMED) newScene.resume();
            }
        }
    }

    @Override
    public void create() {
        state = State.CREATED;
        if (currentScene != null) currentScene.create();
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        if (currentScene != null) {
            currentScene.resize(width, height);
        }
    }

    @Override
    public void prerender() {
        if (currentScene != null) currentScene.prerender();
    }

    @Override
    public void render() {
        if (currentScene != null) currentScene.render();
    }

    @Override
    public void pause() {
        state = State.PAUSED;
        if (currentScene != null) currentScene.pause();
    }

    @Override
    public void resume() {
        state = State.RESUMED;
        if (currentScene != null) currentScene.resume();
    }

    @Override
    public void dispose() {
        state = State.DISPOSED;
        if (currentScene != null) currentScene.dispose();
    }

    enum State {
        CREATED, RESUMED, PAUSED, DISPOSED
    }
}
