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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.core.math.MeasuringRect;
import com.github.st1hy.core.math.VerticesBoundsUtils;

public class ImageFragment implements Disposable {
    private FrameBuffer fbo;
    private Sprite sprite;
    private PendingSprite pendingSprite;

    private ImageFragment(float[] vertices, Texture image) {
        pendingSprite = new PendingSprite(vertices, image);
    }

    /**
     * Creates new fragment from vertices and image. Cuts vertices that are outside of image bounds.
     * If no vertices remain returns null.
     */
    public static ImageFragment createNewFragment(FloatArray vertices, Texture image) {
        MeasuringRect rect = new MeasuringRect(0, image.getWidth(), 0, image.getHeight());
        FloatArray cutVertices = VerticesBoundsUtils.cropVertices(vertices, rect);
        if (cutVertices != null) {
            return new ImageFragment(cutVertices.toArray(), image);
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

    public void setTransformation(Matrix4 transformation) {
    }

    public void render(SpriteBatch batch) {
        if (sprite != null) {
            batch.enableBlending();
            sprite.draw(batch);
        }
    }

    @Override
    public void dispose() {
        if (fbo != null) fbo.dispose();
    }

    private class PendingSprite {
        private final float[] vertices;
        private final Texture texture;

        public PendingSprite(float[] vertices, Texture texture) {
            this.vertices = vertices;
            this.texture = texture;
        }

        public void setupSprite() {
            if (sprite != null) return;

            EarClippingTriangulator triangulator = new EarClippingTriangulator();
            Polygon polygon = new Polygon(vertices);
            Rectangle bounds = polygon.getBoundingRectangle();
            short[] triangles = triangulator.computeTriangles(vertices).items;
            TextureRegion textureRegion = new TextureRegion(texture, 0, 0, texture.getWidth(), texture.getHeight());
            PolygonRegion polygonRegion = new PolygonRegion(textureRegion, polygon.getVertices(), triangles);
            PolygonSprite polygonSprite = new PolygonSprite(polygonRegion);
            polygonSprite.setColor(Color.GOLD);

            PolygonSpriteBatch fb = new PolygonSpriteBatch();
            int x = MathUtils.floor(bounds.x);
            int y = MathUtils.floor(bounds.y);
            int width = MathUtils.ceil(bounds.width);
            int height = MathUtils.ceil(bounds.height);
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

    }
}
