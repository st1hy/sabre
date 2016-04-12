package com.github.st1hy.sabre.core.injector.module;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.github.st1hy.sabre.core.injector.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {
    private final Activity activity;

    public ActivityModule(@NonNull Activity activity) {
        this.activity = activity;
    }

    @Provides
    @PerActivity
    Activity providesActivity() {
        return activity;
    }
}
