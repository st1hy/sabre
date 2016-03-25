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
import com.github.st1hy.core.math.GeometryUtils2;

public class ImageFragment implements Disposable {
    private FrameBuffer fbo;
    private Sprite sprite;
    private PendingSprite pendingSprite;

    private ImageFragment(Polygon polygon, TextureRectangle textureRectangle) {
        pendingSprite = new PendingSprite(polygon, textureRectangle);
    }

    /**
     * Creates new fragment from an intersection of image and area created by path.
     * If path is located outside of image returns null;
     */
    public static ImageFragment createNewFragment(FloatArray vertices, Texture image) {
        TextureRectangle textureRectangle = new TextureRectangle(image);
        Polygon polygon = new MPolygon(vertices.toArray());
        if (polygon.getBoundingRectangle().overlaps(textureRectangle.getBounds())) {
            return new ImageFragment(polygon, textureRectangle);
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
        private final Polygon polygon;
        private final TextureRectangle textureRectangle;

        public PendingSprite(Polygon polygon, TextureRectangle textureRectangle) {
            this.polygon = polygon;
            this.textureRectangle = textureRectangle;
        }

        public void setupSprite() {
            if (sprite != null) return;

            PolygonSprite polygonSprite = textureRectangle.toPolygonSprite(polygon);
            polygonSprite.setColor(Color.GOLD);

            PolygonSpriteBatch fb = new PolygonSpriteBatch();
            Rectangle intersection = GeometryUtils2.intersect(polygon.getBoundingRectangle(),
                    textureRectangle.getBounds());
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

    }

    private static class MPolygon extends Polygon {
        private Rectangle bounds;

        public MPolygon(float[] vertices) {
            super(vertices);
        }

        @Override
        public Rectangle getBoundingRectangle() {
            if (bounds == null) {
                bounds = super.getBoundingRectangle();
            }
            return bounds;
        }
    }

    private static class TextureRectangle {
        private final Texture texture;
        private Rectangle bounds;

        public TextureRectangle(Texture texture) {
            this.texture = texture;
        }

        public Rectangle getBounds() {
            if (bounds == null) {
                bounds = new Rectangle(0, 0, texture.getWidth(), texture.getHeight());
            }
            return bounds;
        }

        public Texture getTexture() {
            return texture;
        }

        public PolygonSprite toPolygonSprite(Polygon polygon) {
            TextureRegion textureRegion = new TextureRegion(texture);
            short[] triangles = new EarClippingTriangulator().computeTriangles(polygon.getVertices()).items;
            PolygonRegion polygonRegion = new PolygonRegion(textureRegion, polygon.getVertices(), triangles);
           return new PolygonSprite(polygonRegion);
        }
    }
}
