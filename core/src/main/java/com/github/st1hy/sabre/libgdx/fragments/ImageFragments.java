package com.github.st1hy.sabre.libgdx.fragments;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.st1hy.sabre.libgdx.ScreenContext;
import com.github.st1hy.sabre.libgdx.mode.UiMode;
import com.github.st1hy.sabre.libgdx.model.ImageFragmentModel;

import rx.Subscription;
import rx.concurrency.GdxScheduler;
import rx.functions.Action1;

public class ImageFragments implements ImageFragmentCreator, ImageFragmentSelector {
    private final ScreenContext model;
    private final Array<ImageFragment> fragments;
    private ImageFragment currentFragment = null;
    private Subscription uiModeSubscription;

    private Vector3 tempVector3 = new Vector3();

    public ImageFragments(ScreenContext model) {
        this.model = model;
        Array<ImageFragmentModel> fragmentModels = model.getFragmentModels();
        this.fragments = new Array<>(fragmentModels.size);
        for (ImageFragmentModel fragmentModel : fragmentModels) {
            ImageFragment fragment = ImageFragment.createNewFragment(fragmentModel, model);
            if (fragment == null) throw new UnknownError("Model could not recreate fragment");
            fragments.add(fragment);
        }
        uiModeSubscription = UiMode.toObservable()
                .observeOn(GdxScheduler.get())
                .subscribe(new Action1<UiMode>() {
                    @Override
                    public void call(UiMode newUiMode) {
                        if (newUiMode != UiMode.MOVE_ELEMENT) {
                            if (changeCurrentFragment(null)) {
                                Gdx.graphics.requestRendering();
                            }
                        }
                    }
                });
    }

    @Override
    public boolean addNew(float[] vertices) {
        ImageFragment fragment = ImageFragment.createNewFragment(vertices, model);
        if (fragment != null) {
            fragments.add(fragment);
            model.getFragmentModels().add(fragment.getModel());
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
        uiModeSubscription.unsubscribe();
        for (int i = 0; i < fragments.size; ++i) {
            fragments.get(i).dispose();
        }
        fragments.clear();
        fragments.shrink();
    }

    private boolean changeCurrentFragment(ImageFragment newImageFragment) {
        boolean isFragmentChanged = isObjectChanged(currentFragment, newImageFragment);
        if (currentFragment != null) {
            currentFragment.setElevation(model.getElevation().getElevationLow());
        }
        currentFragment = newImageFragment;
        if (currentFragment != null) {
            currentFragment.setElevation(model.getElevation().getElevationHigh());
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
            tempVector3.set(screenX, screenY, 0).mul(model.getWorldTransformation().getInvTransformation());
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
