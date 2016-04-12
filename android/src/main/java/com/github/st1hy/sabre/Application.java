package com.github.st1hy.sabre;

import com.github.st1hy.gesturedetector.Config;
import com.github.st1hy.sabre.core.injector.component.AppComponent;
import com.github.st1hy.sabre.core.injector.component.DaggerAppComponent;
import com.github.st1hy.sabre.core.injector.module.ApplicationModule;

import timber.log.Timber;

public class Application extends android.app.Application {
    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Config.DEBUG = BuildConfig.DEBUG;

        component = DaggerAppComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        component.inject(this);
    }

    public AppComponent getComponent() {
        return component;
    }
}
