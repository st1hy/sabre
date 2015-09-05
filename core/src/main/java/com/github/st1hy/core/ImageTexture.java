package com.github.st1hy.core;

import com.badlogic.gdx.graphics.Texture;

public class ImageTexture {
    private final Texture texture;
    private float alpha;

    public ImageTexture(Texture texture) {
        this.texture = texture;
        alpha = 1f;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
