package com.github.st1hy.sabre.libgdx.fragments;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.github.st1hy.coregdx.Transformation;
import com.github.st1hy.sabre.libgdx.mode.UiMode;
import com.github.st1hy.sabre.libgdx.mode.UiModeChangeListener;

public class ImageFragments implements ImageFragmentCreator, UiModeChangeListener, ImageFragmentSelector {
    private final Texture image;
    private final Transformation worldTransformation;
    private Array<ImageFragment> fragments = new Array<>();
    private ImageFragment currentFragment = null;
    private Elevation elevation;

    private Vector3 tempVector3 = new Vector3();

    /**
     * @param image               borrowed texture
     * @param worldTransformation transformation of the world
     */
    public ImageFragments(Texture image, Transformation worldTransformation) {
        this.image = image;
        this.worldTransformation = worldTransformation;
        elevation = new Elevation(Gdx.graphics.getDensity());
        UiMode.registerChangeListener(this);
    }

    @Override
    public boolean addNew(FloatArray outline) {
        ImageFragment fragment = ImageFragment.createNewFragment(outline, image, worldTransformation);
        if (fragment != null) {
            fragments.add(fragment);
            changeCurrentFragment(fragment);
            notifyNewUiMode(UiMode.MOVE_ELEMENT);
            return true;
        } else {
            return false;
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
        UiMode.unregisterChangeListener(this);
    }

    @Override
    public void onUiModeChanged(UiMode newUiMode) {
        if (newUiMode != UiMode.MOVE_ELEMENT) {
            if (changeCurrentFragment(null)) {
                Gdx.graphics.requestRendering();
            }
        }
    }

    private boolean changeCurrentFragment(ImageFragment newImageFragment) {
        boolean isFragmentChanged = isObjectChanged(currentFragment, newImageFragment);
        if (currentFragment != null) {
            currentFragment.setElevation(elevation.getElevationLow());
        }
        currentFragment = newImageFragment;
        if (currentFragment != null) {
            currentFragment.setElevation(elevation.getElevationHigh());
        }
        return isFragmentChanged;
    }

    private static boolean isObjectChanged(Object a, Object b) {
        return a == null && b != null || a != null && b == null || a != null && a != b;
    }

    @Override
    public void onClickedOnImage(float screenX, float screenY) {
        ImageFragment fragmentToChangeTo = null;
        if (fragments.size > 0) {
            tempVector3.set(screenX, screenY, 0).mul(worldTransformation.getInvTransformation());
            float x = tempVector3.x;
            float y = tempVector3.y;
            for (int i = fragments.size - 1; i >= 0; --i) {
                ImageFragment fragment = fragments.get(i);
                if (fragment.isWithinBounds(x, y)) {
                    fragmentToChangeTo = fragment;
                    break;
                }
            }
        }
        boolean isFragmentChanged = changeCurrentFragment(fragmentToChangeTo);
        notifyNewUiMode(fragmentToChangeTo != null ? UiMode.MOVE_ELEMENT : UiMode.MOVE_CAMERA);
        if (isFragmentChanged) {
            Gdx.graphics.requestRendering();
        }
    }

    private static void notifyNewUiMode(UiMode mode) {
        UiMode.setGlobalMode(mode);
    }


    public ImageFragment getCurrentFragment() {
        return currentFragment;
    }
}
