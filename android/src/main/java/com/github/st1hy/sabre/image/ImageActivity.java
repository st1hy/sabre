package com.github.st1hy.sabre.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.github.st1hy.core.utils.SystemUIMode;
import com.github.st1hy.core.utils.Utils;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.ui.BaseActivity;
import com.github.st1hy.sabre.dao.DaoSession;
import com.github.st1hy.sabre.dao.OpenImageUtils;
import com.github.st1hy.sabre.image.inject.DaggerImageActivityComponent;
import com.github.st1hy.sabre.image.inject.ImageActivityComponent;
import com.github.st1hy.sabre.image.inject.ImageActivityModule;
import com.github.st1hy.sabre.libgdx.mode.UiMode;
import com.rey.material.widget.FloatingActionButton;

import java.util.Date;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class ImageActivity extends BaseActivity implements AndroidFragmentApplication.Callbacks {
    private static final String SAVE_UI_MODE_STATE = "ImageActivity.uiMode";
    private UiMode uiMode = UiMode.DEFAULT;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    Uri imageUriFromIntent;

    @Bind(R.id.image_fab)
    FloatingActionButton floatingButton;

    @Inject
    Lazy<DaoSession> daoSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getImageActivityComponent().inject(this);
        if (!checkUri()) return;
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);
        SystemUIMode.IMMERSIVE_STICKY.apply(getWindow());
        restoreState(savedInstanceState);
    }


    protected ImageActivityComponent getImageActivityComponent() {
        return DaggerImageActivityComponent.builder()
                .activityComponent(getComponent())
                .imageActivityModule(new ImageActivityModule(this))
                .build();
    }

    private boolean checkUri() {
        if (imageUriFromIntent != null) {
            updateDatabaseImageDate();
            return true;
        } else {
            notifyWrongImage(this);
            exit();
            return false;
        }
    }

    private void restoreState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        UiMode mode = (UiMode) savedInstanceState.getSerializable(SAVE_UI_MODE_STATE);
        if (mode != null && mode != UiMode.MOVE_CAMERA) {
            uiMode = mode;
            setEditMode(floatingButton, uiMode, false);
            notifyNewUiMode(uiMode);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_UI_MODE_STATE, uiMode);
    }

    @Nullable
    public Uri getImageUriFromIntent() {
        return imageUriFromIntent;
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        subscriptions.add(UiMode.toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UiMode>() {
                    @Override
                    public void call(UiMode newUiMode) {
                        if (newUiMode != uiMode) {
                            uiMode = newUiMode;
                            setEditMode(floatingButton, uiMode, true);
                        }
                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        subscriptions.unsubscribe();
    }

    public void onImageFailedToLoad() {
        ImageActivity.notifyWrongImage(this);
        deleteImageFromDatabase();
        exit();
    }

    public static void notifyWrongImage(@NonNull Context context) {
        Toast.makeText(context, R.string.incoming_image_cannot_be_read, Toast.LENGTH_LONG).show();
    }

    private void updateDatabaseImageDate() {
        Utils.CACHED_EXECUTOR_POOL.execute(new Runnable() {
            @Override
            public void run() {
                OpenImageUtils.updateOpenedImage(ImageActivity.this, daoSession.get(), imageUriFromIntent, new Date());
            }
        });
    }

    private void deleteImageFromDatabase() {
        Utils.CACHED_EXECUTOR_POOL.execute(new Runnable() {
            @Override
            public void run() {
                OpenImageUtils.removeOpenedImage(ImageActivity.this, daoSession.get(), imageUriFromIntent);
            }
        });
    }

    @OnClick(R.id.image_fab)
    void onFloatingButtonPressed(@NonNull FloatingActionButton fab) {
        if (uiMode != UiMode.MOVE_CAMERA) {
            uiMode = UiMode.MOVE_CAMERA;
        } else {
            uiMode = UiMode.CUT_ELEMENT;
        }
        setEditMode(fab, uiMode, true);
        notifyNewUiMode(uiMode);
    }

    private void setEditMode(@NonNull FloatingActionButton fab, final UiMode mode, boolean animate) {
        int drawableResId = mode != UiMode.MOVE_CAMERA ? R.drawable.ic_clear_white_24dp :
                R.drawable.ic_content_cut_white_24dp;
        Drawable icon = Utils.getDrawable(this, drawableResId);
        fab.setIcon(icon, animate);
    }

    private static void notifyNewUiMode(final UiMode mode) {
        UiMode.setGlobalMode(mode);
    }

}
