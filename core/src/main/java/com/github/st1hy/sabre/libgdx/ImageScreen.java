package com.github.st1hy.sabre.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.github.st1hy.coregdx.OnPathChangedListener;
import com.github.st1hy.coregdx.TouchEventState;
import com.github.st1hy.coregdx.Transformable;
import com.github.st1hy.coregdx.screen.TransformableScreen;
import com.github.st1hy.sabre.libgdx.fragments.ImageFragmentSelector;
import com.github.st1hy.sabre.libgdx.fragments.ImageFragments;

public class ImageScreen implements TransformableScreen {
    private ScreenContext screenContext;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private SelectionRenderer selectionRenderer;
    private ImageFragments imageFragments;

    /**
     * @param screenContext image screen model to be displayed. This reference is owned and disposed by this screen.
     */
    public ImageScreen(ScreenContext screenContext) {
        this.screenContext = screenContext;
        imageFragments = new ImageFragments(this.screenContext);
        selectionRenderer = new SelectionRenderer(this.screenContext, imageFragments);
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
    }

    @Override
    public void resize(int width, int height) {
        int imgWidth = screenContext.getBackground().getWidth();
        int imgHeight = screenContext.getBackground().getHeight();
        float scale = Math.min((float) width / (float) imgWidth,
                (float) height / (float) imgHeight);
        screenContext.resetTransformations();
        screenContext.getWorldTransformation().getInitialTransformation().scale(scale, scale, 1)
                .translate((width / scale - imgWidth) / 2, (height / scale - imgHeight) / 2, 0);
        resetTransformation();
    }

    @Override
    public void applyTransformation(TouchEventState state, Matrix3 matrix3) {
        Transformable toTransform = imageFragments.getCurrentFragment();
        if (toTransform != null) {
            toTransform.applyTransformation(state, matrix3);
        } else {
            TRANSFORM.apply(screenContext.getScreenTransformation(), state, matrix3);
            setupWorldTransformation();
        }
        Gdx.graphics.requestRendering();

    }

    @Override
    public void resetTransformation() {
        screenContext.getScreenTransformation().idt();
        setupWorldTransformation();
        Gdx.graphics.requestRendering();
    }

    private void setupWorldTransformation() {
        screenContext.getWorldTransformation().applyTransformation(screenContext.getScreenTransformation().getTransformation());
    }

    @Override
    public void prerender() {
        imageFragments.prerender();
    }

    @Override
    public void render() {
        if (screenContext.getBackground() == null) return;
        renderImage();
        renderShapes();
    }

    private void renderImage() {
        batch.setTransformMatrix(screenContext.getWorldTransformation().getTransformation());
        batch.begin();
        Texture background = screenContext.getBackground();
        batch.draw(background, 0, 0, background.getWidth(), background.getHeight());
        imageFragments.render(batch);
        batch.end();
    }

    private void renderShapes() {
        Texture background = screenContext.getBackground();
        int width = background.getWidth();
        int height = background.getHeight();
        int x = 0;
        int y = 0;
        shapeRenderer.setTransformMatrix(screenContext.getWorldTransformation().getTransformation());
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
        screenContext.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        imageFragments.dispose();
        selectionRenderer.dispose();
    }
}
