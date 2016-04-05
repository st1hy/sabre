package com.github.st1hy.core.screen.fragments;

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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.core.utils.Transformation;

public class ImageFragment implements Disposable {
    private FrameBuffer fbo;
    private Sprite sprite;
    private PendingSprite pendingSprite;
    private final Transformation fragmentTransformation = new Transformation();
    private final Transformation worldTransformation;
    private Matrix4 imageTransformation = new Matrix4();
    private float elevation = 10f;
    private Matrix4 tempMatrix4 = new Matrix4(), tempShadowMatrix4 = new Matrix4();


    private ImageFragment(Polygon polygon, Texture texture, Rectangle intersection, Transformation worldTransformation) {
        this.worldTransformation = worldTransformation;
        this.pendingSprite = new PendingSprite(polygon, texture, intersection);
    }

    /**
     * Creates new fragment from an intersection of image and area created by path.
     * If path is located outside of image returns null;
     */
    public static ImageFragment createNewFragment(FloatArray vertices, Texture texture, Transformation worldTransformation) {
        Polygon polygon = new Polygon(vertices.toArray());
        Rectangle polygonBounds = polygon.getBoundingRectangle();
        Rectangle textureBounds = new Rectangle(0, 0, texture.getWidth(), texture.getHeight());
        Rectangle intersection = new Rectangle();
        if (Intersector.intersectRectangles(polygonBounds, textureBounds, intersection)) {
            return new ImageFragment(polygon, texture, intersection, worldTransformation);
        } else {
            return null;
        }
    }

    public void prerender() {
        if (sprite == null && pendingSprite != null) {
            pendingSprite.setupSprite();
            pendingSprite = null;
        }
    }

    public Transformation getFragmentTransformation() {
        return fragmentTransformation;
    }

    public void render(SpriteBatch batch) {
        if (sprite != null) {
            batch.enableBlending();
            tempMatrix4.set(worldTransformation.getTransformation()).mul(imageTransformation);
            renderShadow(batch, tempMatrix4);
            batch.setTransformMatrix(tempMatrix4);
            sprite.draw(batch);
        }
    }

    private void renderShadow(SpriteBatch batch, Matrix4 transformation) {
        tempShadowMatrix4.idt().setTranslation(elevation, -elevation, 0).mul(transformation);
        batch.setTransformMatrix(tempShadowMatrix4);
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

    public void setupTransformation() {
        imageTransformation.set(worldTransformation.getInvTransformation())
                .mul(fragmentTransformation.getTransformation())
                .mul(worldTransformation.getTransformation());
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    private class PendingSprite {
        private final Polygon polygon;
        private final Texture texture;
        private final Rectangle intersection;

        public PendingSprite(Polygon polygon, Texture texture, Rectangle intersection) {
            this.polygon = polygon;
            this.texture = texture;
            this.intersection = intersection;
        }

        public void setupSprite() {
            if (sprite != null) return;

            PolygonSprite polygonSprite = createPolygonSprite(texture, polygon);

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

            ImageFragment.this.sprite = new Sprite(fbo.getColorBufferTexture());
            sprite.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            sprite.flip(false, true);
            sprite.translate(x, y);
            ImageFragment.this.fbo = fbo;
        }

        public PolygonSprite createPolygonSprite(Texture texture, Polygon polygon) {
            TextureRegion textureRegion = new TextureRegion(texture);
            short[] triangles = new EarClippingTriangulator().computeTriangles(polygon.getVertices()).items;
            PolygonRegion polygonRegion = new PolygonRegion(textureRegion, polygon.getVertices(), triangles);
            return new PolygonSprite(polygonRegion);
        }

    }
}
