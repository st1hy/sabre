package com.github.st1hy.sabre.image;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.github.st1hy.core.utils.SystemUIMode;
import com.github.st1hy.dao.DaoMaster;
import com.github.st1hy.dao.DaoSession;
import com.github.st1hy.dao.OpenImageUtils;
import com.github.st1hy.imagecache.ImageCacheHandler;
import com.github.st1hy.imagecache.ImageCacheProvider;
import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.NavState;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.DependencyDelegate;
import com.github.st1hy.sabre.settings.EnableOpenGLHolder;

import java.util.Date;

import timber.log.Timber;

public class ImageActivity extends AppCompatActivity implements AndroidFragmentApplication.Callbacks, ImageCacheProvider {
    private ImageCacheHandler imageCacheHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Application app = (Application) getApplication();
        imageCacheHandler = DependencyDelegate.configureImageCacheHandler(app);
        if (savedInstanceState == null) {
            NavState state = EnableOpenGLHolder.isOpenGLEnabled(this) ? NavState.IMAGE_VIEWER_GL : NavState.IMAGE_VIEWER_SURFACE;
            Fragment fragment = state.newInstance();
            Intent intent = getIntent();
            if (intent != null) {
                final Uri imageUriFromIntent = getImageUriFromIntent(intent);
                if (imageUriFromIntent != null) {
                    configureFragment(imageUriFromIntent, fragment);
                }
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_fragment_container, fragment).commit();
        }
        SystemUIMode.IMMERSIVE_STICKY.apply(getWindow());
    }

    private void configureFragment(@NonNull final Uri imageUriFromIntent, @NonNull Fragment fragment) {
        Application app = (Application) getApplication();
        final DaoMaster daoMaster = app.getCache().getInstance(DaoMaster.class);
        if (daoMaster == null) {
            Timber.e("No editable database available, cannot update image access time.");
        } else {
            Application.CACHED_EXECUTOR_POOL.execute(new Runnable() {
                @Override
                public void run() {
                    DaoSession daoSession = daoMaster.newSession();
                    OpenImageUtils.updateOpenedImage(ImageActivity.this, daoSession, imageUriFromIntent, new Date());
                }
            });
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(NavState.ARG_IMAGE_URI, imageUriFromIntent);
        fragment.setArguments(bundle);
    }

    @Nullable
    private Uri getImageUriFromIntent(@NonNull Intent intent) {
        String action = intent.getAction();
        Timber.d("View image: %s", intent.toString());
        if (action.equals(Intent.ACTION_VIEW)) {
            return intent.getData();
        }
        return null;
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    @NonNull
    public ImageCacheHandler getImageCacheHandler() {
        return imageCacheHandler;
    }
}
