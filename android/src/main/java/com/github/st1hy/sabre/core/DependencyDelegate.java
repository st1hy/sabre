package com.github.st1hy.sabre.core;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

import com.github.st1hy.imagecache.ImageCacheHandler;
import com.github.st1hy.imagecache.storage.ImageCacheStorage;
import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.MainActivity;
import com.github.st1hy.sabre.settings.SettingsFragment;

public class DependencyDelegate implements ComponentCallbacks2 {
    private final MainActivity mainActivity;
    private final ImageCacheHandler imageCacheHandler;

    public DependencyDelegate(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        Application app = (Application) mainActivity.getApplication();
        imageCacheHandler = configureImageCacheHandler(app);
        SettingsFragment.loadDefaultSettings(mainActivity, false);
        mainActivity.getApplication().registerComponentCallbacks(this);
    }

    public static ImageCacheHandler configureImageCacheHandler(Application app) {
        ImageCacheStorage storageAdapter = new RestrictiveMapToImageCacheStorageAdapter(app.getCache());
        return ImageCacheHandler.getInstance(app, storageAdapter);
    }

    public void onStart() {
    }

    public void onStop() {
        imageCacheHandler.onStop();
    }

    public void onDestroy() {
        mainActivity.getApplication().unregisterComponentCallbacks(this);
        imageCacheHandler.onDestroy();
    }

    @NonNull
    public ImageCacheHandler getImageCacheHandler() {
        return imageCacheHandler;
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == TRIM_MEMORY_BACKGROUND || level == TRIM_MEMORY_RUNNING_LOW) {
            imageCacheHandler.getCache().clearMemory();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override
    public void onLowMemory() {
    }
}
