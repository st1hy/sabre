package com.github.st1hy.sabre.image;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.github.st1hy.core.mode.UiMode;
import com.github.st1hy.core.mode.UiModeChangeListener;
import com.github.st1hy.core.utils.EventBus;
import com.github.st1hy.core.utils.EventMethod;
import com.github.st1hy.core.utils.SystemUIMode;
import com.github.st1hy.core.utils.Utils;
import com.github.st1hy.sabre.dao.DaoMaster;
import com.github.st1hy.sabre.dao.DaoSession;
import com.github.st1hy.sabre.dao.OpenImageUtils;
import com.github.st1hy.imagecache.ImageCacheHandler;
import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.CacheUtils;
import com.github.st1hy.sabre.core.ImageCacheProvider;
import com.rey.material.widget.FloatingActionButton;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class ImageActivity extends AppCompatActivity implements AndroidFragmentApplication.Callbacks, ImageCacheProvider {
    private ImageCacheHandler imageCacheHandler;
    private Uri imageUriFromIntent;
    @Bind(R.id.image_fab)
    FloatingActionButton floatingButton;
    private static final String SAVE_EDIT_MODE_STATE = "ImageActivity.isInEditMode";
    private boolean isInEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application app = (Application) getApplication();
        imageCacheHandler = CacheUtils.newImageCacheHandler(app);
        Intent intent = getIntent();
        if (intent != null) {
            imageUriFromIntent = getImageUriFromIntent(intent);
            if (imageUriFromIntent != null) {
                updateDatabaseImageDate();
            } else {
                notifyWrongImage(this);
                exit();
                return;
            }
        }
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);
        SystemUIMode.IMMERSIVE_STICKY.apply(getWindow());
        if (savedInstanceState != null) restoreState(savedInstanceState);
    }

    private void restoreState(@NonNull Bundle savedInstanceState) {
        isInEditMode = savedInstanceState.getBoolean(SAVE_EDIT_MODE_STATE, false);
        if (isInEditMode) setEditMode(floatingButton, true, false);
    }

    @Nullable
    private Uri getImageUriFromIntent(@NonNull Intent intent) {
        String action = intent.getAction();
        Timber.d("View image: %s", intent.toString());
        if (action.equals(Intent.ACTION_VIEW)) {
            String type = intent.getType();
            if (type != null && type.startsWith("image/")) {
                return intent.getData();
            }
        } else if (action.equals(Intent.ACTION_SEND)) {
            String type = intent.getType();
            if (type != null && type.startsWith("image/")) {
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                Timber.d("View image extra stream: %s", uri);
                return uri;
            }
        }
        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_EDIT_MODE_STATE, isInEditMode);
    }

    @Nullable
    public Uri getImageUriFromIntent() {
        return imageUriFromIntent;
    }

    @Override
    public void exit() {
        finish();
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
        final DaoMaster daoMaster = getDaoMaster();
        if (daoMaster == null) return;
        Utils.CACHED_EXECUTOR_POOL.execute(new Runnable() {
            @Override
            public void run() {
                DaoSession daoSession = daoMaster.newSession();
                OpenImageUtils.updateOpenedImage(ImageActivity.this, daoSession, imageUriFromIntent, new Date());
            }
        });
    }

    private void deleteImageFromDatabase() {
        final DaoMaster daoMaster = getDaoMaster();
        if (daoMaster == null) return;
        Utils.CACHED_EXECUTOR_POOL.execute(new Runnable() {
            @Override
            public void run() {
                DaoSession daoSession = daoMaster.newSession();
                OpenImageUtils.removeOpenedImage(ImageActivity.this, daoSession, imageUriFromIntent);
            }
        });
    }

    @Nullable
    private DaoMaster getDaoMaster() {
        Application app = (Application) getApplication();
        final DaoMaster daoMaster = app.getCache().getInstance(DaoMaster.class);
        if (daoMaster == null) {
            Timber.wtf("No editable database available!");
        }
        return daoMaster;
    }

    @Override
    @NonNull
    public ImageCacheHandler getImageCacheHandler() {
        return imageCacheHandler;
    }

    @OnClick(R.id.image_fab)
    void onFloatingButtonPressed(@NonNull FloatingActionButton fab) {
        isInEditMode = !isInEditMode;
        setEditMode(fab, isInEditMode, true);
    }

    private void setEditMode(@NonNull FloatingActionButton fab, boolean isInEdit, boolean animate) {
        int drawableResId = isInEdit ? R.drawable.ic_clear_white_24dp :
                R.drawable.ic_content_cut_white_24dp;
        Drawable icon = Utils.getDrawable(this, drawableResId);
        fab.setIcon(icon, animate);
        final UiMode mode = isInEdit ? UiMode.CUT_ELEMENT : UiMode.MOVE_CAMERA;
        EventBus.INSTANCE.apply(UiModeChangeListener.class, new EventMethod<UiModeChangeListener>() {
            @Override
            public void apply(@Nullable UiModeChangeListener input) {
                if (input != null) input.onUiModeChanged(mode);
            }
        });
    }
}
