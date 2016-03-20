package com.github.st1hy.core.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class ImageScreen extends AbstractScreen {
    private SpriteBatch batch;
    private int startX,startY, imgWidthOut, imgHeightOut;
    private Texture texture;

    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private OrthographicCamera camera = new OrthographicCamera();
    private int width, height;

    /**
     * @param texture texture to be displayed. This reference is not managed by this scene.
     */
    public ImageScreen(Texture texture) {
        super();
        this.texture = texture;
    }

    @Override
    public void create() {
        super.create();
        batch = new SpriteBatch();
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
        camera.setToOrtho(false, width, height);
    }

    @Override
    protected void setTransformation(Matrix4 matrix4, Quaternion rotation, Vector3 translation, float scaleZ) {
//        camera.transform(matrix4);
        batch.getTransformMatrix().set(matrix4);
        shapeRenderer.setTransformMatrix(matrix4);
    }

    @Override
    public void render() {
        if (texture == null) return;
        camera.update();
        renderImage();
        renderShapes();
    }

    private void renderImage() {
//        batch.getProjectionMatrix().set(camera.combined);
        batch.begin();
        batch.draw(texture, startX, startY, imgWidthOut, imgHeightOut);
        batch.end();
    }

    private void renderShapes() {
//        shapeRenderer..set(camera.combined);
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
