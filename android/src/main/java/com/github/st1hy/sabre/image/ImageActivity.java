package com.github.st1hy.sabre.image;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.github.st1hy.core.utils.SystemUIMode;
import com.github.st1hy.imagecache.CacheHandler;
import com.github.st1hy.imagecache.CacheProvider;
import com.github.st1hy.retainer.Retainer;
import com.github.st1hy.sabre.NavState;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.settings.EnableOpenGLHolder;

public class ImageActivity extends AppCompatActivity implements AndroidFragmentApplication.Callbacks, CacheProvider {
    private CacheHandler cacheHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            NavState state = EnableOpenGLHolder.isOpenGLEnabled(this) ? NavState.IMAGE_VIEWER_GL : NavState.IMAGE_VIEWER_SURFACE;
            Fragment fragment = state.newInstance();
            Intent intent = getIntent();
            if (intent != null) {
                fragment.setArguments(intent.getExtras());
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_fragment_container, fragment).commit();
        }
        SystemUIMode.IMMERSIVE_STICKY.apply(getWindow());
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    @NonNull
    public CacheHandler getCacheHandler() {
        if (cacheHandler == null) {
            synchronized (this) {
                if (cacheHandler == null) {
                    cacheHandler = CacheHandler.getInstance(getApplicationContext(), (Retainer) getApplication());
                }
            }
        }
        return cacheHandler;
    }
}
