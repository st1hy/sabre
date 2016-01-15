package com.github.st1hy.sabre;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.github.st1hy.sabre.image.gdx.GdxImageViewerFragment;
import com.github.st1hy.sabre.history.HistoryFragment;
import com.github.st1hy.sabre.image.surface.SurfaceImageViewFragment;
import com.github.st1hy.sabre.settings.SettingsFragment;

public enum NavState {
    HISTORY(HistoryFragment.class),
    IMAGE_VIEWER_GL(GdxImageViewerFragment.class),
    IMAGE_VIEWER_SURFACE(SurfaceImageViewFragment.class),
    SETTINGS(SettingsFragment.class),
    ;
    public static final String ARG_IMAGE_URI = "com.github.st1hy.sabre.image_uri";

    private static final String TAG = "Navigation state";

    private final Class<? extends Fragment> mClass;

    NavState(Class<? extends Fragment> mClass) {
        this.mClass = mClass;
    }

    @NonNull
    public Fragment newInstance() {
        Exception exception;
        try {
            return mClass.newInstance();
        } catch (InstantiationException e) {
            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        }
        throw new IllegalStateException("Error occurred during fragment initialization: "+ mClass.getCanonicalName(), exception);
    }
}
