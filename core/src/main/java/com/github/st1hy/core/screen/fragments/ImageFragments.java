package com.github.st1hy.core.screen.fragments;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

public class ImageFragments implements ImageFragmentCreator {
    private final Texture image;
    private Array<ImageFragment> fragments = new Array<ImageFragment>();

    /**
     * @param image borrowed texture
     */
    public ImageFragments(Texture image) {
        this.image = image;
    }

    @Override
    public boolean addNew(FloatArray outline) {
        ImageFragment fragment = ImageFragment.createNewFragment(outline, image);
        if (fragment != null) {
            fragments.add(fragment);
            return true;
        } else {
            return false;
        }
    }

    public void setTransformation(Matrix4 screenTransformation) {
        for (int i = 0; i < fragments.size; ++i) {
            fragments.get(i).setTransformation(screenTransformation);
        }
    }

    public void prerender() {
        for (int i = 0; i < fragments.size; ++i) {
            fragments.get(i).prerender();
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < fragments.size; ++i) {
            fragments.get(i).render(batch);
        }
    }

    public void dispose() {
        for (int i = 0; i < fragments.size; ++i) {
            fragments.get(i).dispose();
        }
        fragments.clear();
        fragments.shrink();
    }

}
