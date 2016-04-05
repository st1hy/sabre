package com.github.st1hy.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.github.st1hy.core.Matrix3ChangedListener;
import com.github.st1hy.core.State;
import com.github.st1hy.core.mode.UiMode;
import com.github.st1hy.core.mode.UiModeChangeListener;
import com.github.st1hy.core.screen.fragments.ImageFragments;
import com.github.st1hy.core.screen.path.OnPathChangedListener;
import com.github.st1hy.core.screen.path.PathRenderer;
import com.github.st1hy.core.utils.EventBus;
import com.github.st1hy.core.utils.Transformation;

public class ImageScreen implements UiModeChangeListener, TransformableScreen {
    private Texture texture;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private PathRenderer pathRenderer;
    private ImageFragments imageFragments;

    private Transformation screenTransformation = new Transformation();
    private Transformation worldTransformation = new Transformation();
    private UiMode uiMode = UiMode.DEFAULT;

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
        return pathRenderer;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        batch.disableBlending();
        shapeRenderer = new ShapeRenderer();
        imageFragments = new ImageFragments(texture, worldTransformation);
        pathRenderer = new PathRenderer(imageFragments, worldTransformation);
        screenTransformation.idt();
        worldTransformation.idt();
        EventBus.INSTANCE.add(UiModeChangeListener.class, this);
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
    public void onMatrix3Changed(State state, Matrix3 matrix3) {
        Transformation transformation = null;
        if (uiMode == UiMode.MOVE_CAMERA) {
            transformation = screenTransformation;
        } else if (uiMode == UiMode.MOVE_ELEMENT) {
            transformation = imageFragments.getCurrentFragmentTransformation();
        }
        if (transformation != null) {
            if (state == State.STARTED) {
                transformation.applyTransformationRelative(matrix3);
            } else {
                transformation.applyTransformation(matrix3);
            }
        }
        if (uiMode == UiMode.MOVE_CAMERA) {
            setupWorldTransformation();
        } else if (uiMode == UiMode.MOVE_ELEMENT) {
            imageFragments.setupCurrentFragmentTransformation();
        }
        Gdx.graphics.requestRendering();
    }

    @Override
    public void onMatrix3Reset() {
        resetWorld();
    }

    private void resetWorld() {
        screenTransformation.idt();
        setupWorldTransformation();
    }

    private void setupWorldTransformation() {
        worldTransformation.applyTransformation(screenTransformation.getTransformation());
    }

    @Override
    public void onUiModeChanged(UiMode newUiMode) {
        Gdx.app.debug("IMAGE_SCREEN", "Mode: " + newUiMode);
        uiMode = newUiMode;
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
        imageFragments.dispose();
        pathRenderer.dispose();
        EventBus.INSTANCE.remove(UiModeChangeListener.class, this);
    }
}
