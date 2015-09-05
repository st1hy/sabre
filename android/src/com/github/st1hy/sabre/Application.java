package com.github.st1hy.sabre;

import com.github.st1hy.gesturedetector.Config;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Config.DEBUG = BuildConfig.DEBUG;
    }
}
