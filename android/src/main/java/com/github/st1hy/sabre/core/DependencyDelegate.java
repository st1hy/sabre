package com.github.st1hy.sabre.core;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;

import com.github.st1hy.sabre.MainActivity;
import com.github.st1hy.sabre.SettingsFragment;
import com.github.st1hy.sabre.core.cache.retainer.Retainer;

public class DependencyDelegate implements ComponentCallbacks2{
    private final MainActivity mainActivity;
    private final CacheHandler cacheHandler;

    public DependencyDelegate(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        cacheHandler = CacheHandler.getInstance(mainActivity.getApplicationContext(), (Retainer) mainActivity.getApplication());
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
