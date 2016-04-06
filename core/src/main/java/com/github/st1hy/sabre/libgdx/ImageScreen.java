package com.github.st1hy.sabre.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.github.st1hy.coregdx.Matrix3ChangedListener;
import com.github.st1hy.coregdx.OnPathChangedListener;
import com.github.st1hy.coregdx.TouchEventState;
import com.github.st1hy.coregdx.Transformation;
import com.github.st1hy.coregdx.screen.TransformableScreen;
import com.github.st1hy.sabre.libgdx.fragments.ImageFragmentSelector;
import com.github.st1hy.sabre.libgdx.fragments.ImageFragments;

public class ImageScreen implements TransformableScreen {
    private Texture texture;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private SelectionRenderer selectionRenderer;
    private ImageFragments imageFragments;

    private Transformation screenTransformation = new Transformation();
    private Transformation worldTransformation = new Transformation();

    /**
     * @param texture texture to be displayed. This reference is owned and disposed by this screen.
     */
    public ImageScreen(Texture texture) {
        super();
        this.texture = texture;
    }

    public Matrix3ChangedListener getScreenTransformationListener() {
        return this;
    }

    public OnPathChangedListener getPathDrawingListener() {
        return selectionRenderer;
    }

    public ImageFragmentSelector getImageFragmentSelector() {
        return imageFragments;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        batch.disableBlending();
        shapeRenderer = new ShapeRenderer();
        imageFragments = new ImageFragments(texture, worldTransformation);
        selectionRenderer = new SelectionRenderer(imageFragments, worldTransformation);
        screenTransformation.idt();
        worldTransformation.idt();
    }

    @Override
    public void resize(int width, int height) {
        int imgWidth = texture.getWidth();
        int imgHeight = texture.getHeight();
        float scale = Math.min((float) width / (float) imgWidth,
                (float) height / (float) imgHeight);
        worldTransformation.idt();
        worldTransformation.getInitialTransformation().scale(scale, scale, 1)
                .translate((width / scale - imgWidth) / 2, (height / scale - imgHeight) / 2, 0);
        resetWorld();
    }

    @Override
    public void onMatrix3Changed(TouchEventState state, Matrix3 matrix3) {
        Transformation transformation = imageFragments.getCurrentFragmentTransformation();
        if (transformation == null) {
            applyTransformation(screenTransformation, state, matrix3);
            setupWorldTransformation();
        } else {
            applyTransformation(transformation, state, matrix3);
            imageFragments.setupCurrentFragmentTransformation();
        }
        Gdx.graphics.requestRendering();
    }

    private void applyTransformation(Transformation transformation, TouchEventState state, Matrix3 matrix3) {
        if (state == TouchEventState.STARTED) {
            transformation.applyTransformationRelative(matrix3);
        } else {
            transformation.applyTransformation(matrix3);
        }
    }

    @Override
    public void onMatrix3Reset() {
        resetWorld();
    }

    private void resetWorld() {
        screenTransformation.idt();
        setupWorldTransformation();
        Gdx.graphics.requestRendering();
    }

    private void setupWorldTransformation() {
        worldTransformation.applyTransformation(screenTransformation.getTransformation());
    }

    @Override
    public void prerender() {
        imageFragments.prerender();
    }

    @Override
    public void render() {
        if (texture == null) return;
        renderImage();
        renderShapes();
    }

    private void renderImage() {
        batch.setTransformMatrix(worldTransformation.getTransformation());
        batch.begin();
        batch.draw(texture, 0, 0, texture.getWidth(), texture.getHeight());
        imageFragments.render(batch);
        batch.end();
    }

    private void renderShapes() {
        int width = texture.getWidth();
        int height = texture.getHeight();
        int x = 0;
        int y = 0;
        shapeRenderer.setTransformMatrix(worldTransformation.getTransformation());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(x, y, x + width, y + height);
        shapeRenderer.line(x, y + height, x + width, y);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.circle(x + width / 2, y + height / 2, Math.min(width / 2, height / 2));
        selectionRenderer.render(shapeRenderer);
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
        imageFragments.dispose();
        selectionRenderer.dispose();
    }
}
