package com.github.st1hy.core.screen.fragments;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.core.mode.UiMode;
import com.github.st1hy.core.mode.UiModeChangeListener;
import com.github.st1hy.core.utils.EventBus;
import com.github.st1hy.core.utils.EventMethod;
import com.github.st1hy.core.utils.Transformation;

public class ImageFragments implements ImageFragmentCreator {
    private final Texture image;
    private final Transformation worldTransformation;
    private Array<ImageFragment> fragments = new Array<ImageFragment>();
    private ImageFragment currentFragment = null;

    /**
     * @param image borrowed texture
     * @param worldTransformation transformation of the world
     */
    public ImageFragments(Texture image, Transformation worldTransformation) {
        this.image = image;
        this.worldTransformation = worldTransformation;
    }

    @Override
    public boolean addNew(FloatArray outline) {
        ImageFragment fragment = ImageFragment.createNewFragment(outline, image, worldTransformation);
        if (fragment != null) {
            fragments.add(fragment);
            currentFragment = fragment;
            EventBus.INSTANCE.apply(UiModeChangeListener.class, setObjectMode);
            return true;
        } else {
            return false;
        }
    }

    private final EventMethod<UiModeChangeListener> setObjectMode = new EventMethod<UiModeChangeListener>() {
        @Override
        public void apply(UiModeChangeListener uiModeChangeListener) {
            if (uiModeChangeListener != null) uiModeChangeListener.onUiModeChanged(UiMode.MOVE_ELEMENT);
        }
    };

    public Transformation getCurrentFragmentTransformation() {
        if (currentFragment != null) {
            return currentFragment.getFragmentTransformation();
        } else {
            return null;
        }
    }

    public void setupCurrentFragmentTransformation() {
        if (currentFragment != null) {
            currentFragment.setupTransformation();
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
