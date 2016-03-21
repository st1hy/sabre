package com.github.st1hy.core.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.github.st1hy.core.Matrix3ChangedListener;

public class ImageScreen extends AbstractScreen {
    private Texture texture;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private PathRenderer pathRenderer;

    private Matrix4 imageToScreenTransformation = new Matrix4();

    /**
     * @param texture texture to be displayed. This reference is not managed by this scene.
     */
    public ImageScreen(Texture texture) {
        super();
        this.texture = texture;
    }

    public Matrix3ChangedListener getScreenTransformationListener() {
        return this;
    }

    public OnPathChangedListener getPathDrawingListener() {
        return pathRenderer;
    }

    @Override
    public void create() {
        super.create();
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        pathRenderer = new PathRenderer();
    }

    @Override
    public void resize(int width, int height) {
        int imgWidth = texture.getWidth();
        int imgHeight = texture.getHeight();
        float scale = Math.min((float) width / (float) imgWidth,
                (float) height / (float) imgHeight);
        imageToScreenTransformation.idt().scale(scale, scale, 1)
                .translate((width / scale - imgWidth) / 2, (height / scale - imgHeight) / 2, 0);
        setTransformation(batch.getTransformMatrix().idt());
    }

    @Override
    protected void setTransformation(Matrix4 screenTransformation) {
        Matrix4 transform = batch.getTransformMatrix().set(screenTransformation).mul(imageToScreenTransformation);
        shapeRenderer.setTransformMatrix(transform);
        pathRenderer.setTransformation(transform);
    }

    @Override
    public void render() {
        if (texture == null) return;
        renderImage();
        renderShapes();
    }

    private void renderImage() {
        batch.begin();
        batch.draw(texture, 0, 0, texture.getWidth(), texture.getHeight());
        batch.end();
    }

    private void renderShapes() {
        int width = texture.getWidth();
        int height = texture.getHeight();
        int x = 0;
        int y = 0;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(x, y, x + width, y + height);
        shapeRenderer.line(x, y + height, x + width, y);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.circle(x + width / 2, y + height / 2, Math.min(width / 2, height / 2));
        pathRenderer.render(shapeRenderer);
        shapeRenderer.end();
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
        shapeRenderer.dispose();
        pathRenderer.dispose();
    }
}
