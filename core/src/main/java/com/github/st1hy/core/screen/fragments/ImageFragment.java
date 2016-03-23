package com.github.st1hy.core.screen.fragments;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;

public class ImageFragment implements Disposable {
    private FrameBuffer fbo;
    private Sprite sprite;
    private PendingSprite pendingSprite;

    public ImageFragment(FloatArray vertices, Texture image) {
        pendingSprite = new PendingSprite(vertices, image);
    }

    public void prerender() {
        if (sprite == null) {
            pendingSprite.setupSprite();
        }
    }

    public void setTransformation(Matrix4 transformation) {
    }

    public void render(SpriteBatch batch) {
        if (sprite != null) {
            batch.begin();
            batch.enableBlending();
            sprite.draw(batch);
            batch.end();
        }
    }

    @Override
    public void dispose() {
        if (fbo != null) fbo.dispose();
    }

    private class PendingSprite {
        private final float[] vertices;
        private final Texture texture;

        public PendingSprite(FloatArray vertices, Texture texture) {
            this.vertices = vertices.toArray();
            this.texture = texture;
        }

        public void setupSprite() {
            if (sprite != null) return;

            EarClippingTriangulator triangulator = new EarClippingTriangulator();
            Polygon polygon = new Polygon(vertices);
//            Rectangle bounds = polygon.getBoundingRectangle();
            int width = texture.getWidth();
            int height =  texture.getHeight();
            short[] triangles = triangulator.computeTriangles(vertices).items;
            TextureRegion textureRegion = new TextureRegion(texture, 0, 0, width, height);
            PolygonRegion polygonRegion = new PolygonRegion(textureRegion, polygon.getVertices(), triangles);
            PolygonSprite polygonSprite = new PolygonSprite(polygonRegion);
//            polygonSprite.setColor(Color.GOLD);


            PolygonSpriteBatch fb = new PolygonSpriteBatch();
            fb.getProjectionMatrix().setToOrtho2D(0, 0, width, height);

            FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

            fbo.begin();

//            fb.enableBlending();
//            Gdx.gl.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

            Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            fb.begin();

//            fb.draw(texture, vertices, 0, vertices.length, triangles, 0, triangles.length);
            polygonSprite.draw(fb);

            fb.end();

            fbo.end();

            ImageFragment.this.sprite = new Sprite(fbo.getColorBufferTexture());
            sprite.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            sprite.flip(false, true);
            sprite.translate(0, 100);//Example value, so its visible
            ImageFragment.this.fbo = fbo;
            pendingSprite = null;
        }
    }
}
