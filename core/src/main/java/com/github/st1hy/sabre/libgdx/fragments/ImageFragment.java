package com.github.st1hy.sabre.libgdx.fragments;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.github.st1hy.coregdx.TouchEventState;
import com.github.st1hy.coregdx.Transformable;
import com.github.st1hy.sabre.libgdx.ScreenContext;
import com.github.st1hy.sabre.libgdx.model.ImageFragmentModel;

public class ImageFragment implements Disposable, Transformable {
    private final ImageFragmentModel model;
    private final ScreenContext screenModel;

    private Rectangle intersection;
    private Polygon polygon;

    private FrameBuffer fbo = null;
    private Sprite sprite = null;
    private float elevation;

    private Matrix4 fragmentMatrix = new Matrix4(), imagePartialMatrix = new Matrix4(), shadowMatrix = new Matrix4();
    private Vector3 tempVector3 = new Vector3();


    private ImageFragment(ImageFragmentModel model, ScreenContext screenModel) {
        this.model = model;
        this.screenModel = screenModel;
        this.elevation = screenModel.getElevation().getElevationLow();
    }

    /**
     * Creates new fragment from an intersection of image and area created by path.
     * If path is located outside of image returns null;
     */
    public static ImageFragment createNewFragment(float[] vertices, ScreenContext screenModel) {
        Polygon polygon = new Polygon(vertices);
        Rectangle polygonBounds = polygon.getBoundingRectangle();
        Texture texture = screenModel.getBackground();
        Rectangle textureBounds = new Rectangle(0, 0, texture.getWidth(), texture.getHeight());
        Rectangle intersection = new Rectangle();
        if (Intersector.intersectRectangles(polygonBounds, textureBounds, intersection)) {
            ImageFragmentModel model = new ImageFragmentModel(vertices);
            ImageFragment fragment = new ImageFragment(model, screenModel);
            fragment.polygon = polygon;
            fragment.intersection = intersection;
            return fragment;
        } else {
            return null;
        }
    }

    /**
     * Creates new fragment from an intersection of image and area created by path.
     * If path is located outside of image returns null;
     */
    public static ImageFragment createNewFragment(ImageFragmentModel model, ScreenContext screenModel) {
        Polygon polygon = new Polygon(model.getVertices());
        Rectangle polygonBounds = polygon.getBoundingRectangle();
        Texture texture = screenModel.getBackground();
        Rectangle textureBounds = new Rectangle(0, 0, texture.getWidth(), texture.getHeight());
        Rectangle intersection = new Rectangle();
        if (Intersector.intersectRectangles(polygonBounds, textureBounds, intersection)) {
            ImageFragment fragment = new ImageFragment(model, screenModel);
            fragment.polygon = polygon;
            fragment.intersection = intersection;
            return fragment;
        } else {
            return null;
        }
    }

    public ImageFragmentModel getModel() {
        return model;
    }

    public void prerender() {
        if (sprite == null ) {
            getSpriteLazy();
        }
    }

    public void render(SpriteBatch batch) {
        if (sprite != null) {
            batch.enableBlending();
            Matrix4 worldMatrix = screenModel.getWorldTransformation().getTransformation();
            Matrix4 imageMatrix = model.getImageTransformation().getTransformation();
            fragmentMatrix.set(worldMatrix).mul(imageMatrix);
            renderShadow(batch, fragmentMatrix);
            batch.setTransformMatrix(fragmentMatrix);
            sprite.draw(batch);
        }
    }

    private void renderShadow(SpriteBatch batch, Matrix4 transformation) {
        shadowMatrix.idt().setTranslation(elevation, -elevation, 0).mul(transformation);
        batch.setTransformMatrix(shadowMatrix);
        sprite.setColor(Color.BLACK);
        sprite.setAlpha(0.3f);
        sprite.draw(batch);
        sprite.setColor(Color.WHITE);
        sprite.setAlpha(1f);
    }


    @Override
    public void dispose() {
        if (fbo != null) fbo.dispose();
    }


    /**
     * Converts transformation of the fragment in screen coordinates into the same transformation in
     * image coordinates so when user changes screen transformation position of the fragment remains intact.
     */
    @Override
    public void applyTransformation(TouchEventState state, Matrix3 matrix3) {
        imagePartialMatrix.set(matrix3)
                .mulLeft(screenModel.getWorldTransformation().getInvTransformation())
                .mul(screenModel.getWorldTransformation().getTransformation());
        TRANSFORM.apply(model.getImageTransformation(), state, imagePartialMatrix);
    }

    @Override
    public void resetTransformation() {
        model.getImageTransformation().idt();
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    /**
     * x,y coordinates are in image space
     */
    public boolean isWithinBounds(float x, float y) {
        tempVector3.set(x, y, 0).mul(model.getImageTransformation().getInvTransformation());
        x = tempVector3.x;
        y = tempVector3.y;

        float[] vertices = polygon.getVertices();
        boolean isWithinBounds = intersection.contains(x, y);
        boolean isInPolygon = false;
        if (isWithinBounds) {
            isInPolygon = Intersector.isPointInPolygon(vertices, 0, vertices.length, x, y);
        }
        return isWithinBounds && isInPolygon;
    }

    public Sprite getSpriteLazy() {
        if (sprite != null) return sprite;

        PolygonSprite polygonSprite = createPolygonSprite(screenModel.getBackground(), polygon);

        PolygonSpriteBatch fb = new PolygonSpriteBatch();
        int x = MathUtils.floor(intersection.x);
        int y = MathUtils.floor(intersection.y);
        int width = MathUtils.ceil(intersection.width);
        int height = MathUtils.ceil(intersection.height);
        FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fb.getProjectionMatrix().setToOrtho2D(x, y, width, height);

        fbo.begin();

        fb.enableBlending();
        Gdx.gl.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fb.begin();

        polygonSprite.draw(fb);

        fb.end();

        fbo.end();

        Sprite sprite = new Sprite(fbo.getColorBufferTexture());
        sprite.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        sprite.flip(false, true);
        sprite.translate(x, y);

        this.sprite = sprite;
        this.fbo = fbo;
        return sprite;
    }

    private static PolygonSprite createPolygonSprite(Texture texture, Polygon polygon) {
        TextureRegion textureRegion = new TextureRegion(texture);
        short[] triangles = new EarClippingTriangulator().computeTriangles(polygon.getVertices()).items;
        PolygonRegion polygonRegion = new PolygonRegion(textureRegion, polygon.getVertices(), triangles);
        return new PolygonSprite(polygonRegion);
    }
}
