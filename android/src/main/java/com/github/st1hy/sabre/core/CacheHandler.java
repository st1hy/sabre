package com.github.st1hy.sabre.core;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

import com.github.st1hy.sabre.core.cache.ImageCache;
import com.github.st1hy.sabre.core.cache.retainer.Retainer;
import com.github.st1hy.sabre.core.cache.retainer.SupportRetainFragment;

import java.util.concurrent.Executor;

public class CacheHandler {
    private final FragmentActivity activity;
    private final ImageCache cache;
    private final Executor executor = AsyncTask.THREAD_POOL_EXECUTOR;

    CacheHandler(FragmentActivity activity) {
        this.activity = activity;
        this.cache = initCache();
    }

    public ImageCache getCache() {
        return cache;
    }

    private ImageCache initCache() {
        ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams(activity, "images");
        params.diskCacheEnabled = false;
        params.setMemCacheSizePercent(0.25f);
        Retainer retainer = SupportRetainFragment.findOrCreateRetainFragment(activity.getSupportFragmentManager());
        final ImageCache imageCache = ImageCache.getInstance(retainer, params);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                imageCache.initDiskCache();
            }
        });
        return imageCache;
    }

    void onStop() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                cache.flush();
            }
        });
    }

    void onDestroy() {
        cache.close();
    }
}
