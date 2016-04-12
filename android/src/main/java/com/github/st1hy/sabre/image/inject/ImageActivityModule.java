package com.github.st1hy.sabre.image.inject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import dagger.Module;
import dagger.Provides;
import timber.log.BuildConfig;
import timber.log.Timber;

@Module
public class ImageActivityModule {
    private final Activity activity;

    public ImageActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @PerImageActivity
    public Uri provideUri() {
        Intent intent = activity.getIntent();
        if (intent == null) return null;
        String action = intent.getAction();
        if (BuildConfig.DEBUG) {
            Timber.d("View image: %s", intent.toString());
        }
        if (action.equals(Intent.ACTION_VIEW)) {
            String type = intent.getType();
            if (type != null && type.startsWith("image/")) {
                return intent.getData();
            }
        } else if (action.equals(Intent.ACTION_SEND)) {
            String type = intent.getType();
            if (type != null && type.startsWith("image/")) {
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (BuildConfig.DEBUG) {
                    Timber.d("View image extra stream: %s", uri);
                }
                return uri;
            }
        }
        return null;
    }
}
