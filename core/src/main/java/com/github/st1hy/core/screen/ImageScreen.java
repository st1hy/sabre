package com.github.st1hy.core.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.github.st1hy.core.Matrix3ChangedListener;

public class ImageScreen extends AbstractScreen {
    private int startX,startY, imgWidthOut, imgHeightOut;
    private Texture texture;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private PathRenderer pathRenderer;
    private int width, height;

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
        this.width = width;
        this.height = height;
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
    protected void setTransformation(Matrix4 matrix4, Quaternion rotation, Vector3 translation, float scaleZ) {
        batch.getTransformMatrix().set(matrix4);
        shapeRenderer.setTransformMatrix(matrix4);
    }

    @Override
    public void render() {
        if (texture == null) return;
        renderImage();
        renderShapes();
    }

    private void renderImage() {
        batch.begin();
        batch.draw(texture, startX, startY, imgWidthOut, imgHeightOut);
        batch.end();
    }

    private void renderShapes() {
        int width = imgWidthOut;
        int height = imgHeightOut;
        int x = startX;
        int y = startY;
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
    }
}
