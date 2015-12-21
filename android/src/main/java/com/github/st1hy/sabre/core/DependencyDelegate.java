package com.github.st1hy.sabre.core;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

import com.github.st1hy.imagecache.CacheHandler;
import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.MainActivity;
import com.github.st1hy.sabre.SettingsFragment;

public class DependencyDelegate implements ComponentCallbacks2 {
    private final MainActivity mainActivity;
    private final CacheHandler cacheHandler;

    public DependencyDelegate(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        Application app = (Application) mainActivity.getApplication();
        cacheHandler = CacheHandler.getInstance(app, app);
        SettingsFragment.loadDefaultSettings(mainActivity, false);
        mainActivity.getApplication().registerComponentCallbacks(this);
    }

    public void onStart() {
    }

    public void onStop() {
        cacheHandler.onStop();
    }

    public void onDestroy() {
        mainActivity.getApplication().unregisterComponentCallbacks(this);
        cacheHandler.onDestroy();
    }

    @NonNull
    public CacheHandler getCacheHandler() {
        return cacheHandler;
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == TRIM_MEMORY_BACKGROUND || level == TRIM_MEMORY_RUNNING_LOW) {
            cacheHandler.getCache().clearMemory();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override
    public void onLowMemory() {
    }
}
