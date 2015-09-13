package com.github.st1hy.sabre.core;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.core.cache.ImageCache;
import com.github.st1hy.sabre.core.cache.retainer.RetainFragment;
import com.github.st1hy.sabre.core.cache.retainer.Retainer;
import com.github.st1hy.sabre.core.cache.retainer.SupportRetainFragment;

import java.util.concurrent.Executor;

public class CacheHandler {
    private final ImageCache cache;
    private final Executor executor = Application.CACHED_EXECUTOR_POOL;

    private CacheHandler(Activity activity) {
        this.cache = initCacheFromRetainFragment(activity);
    }

    private CacheHandler(Context context, Retainer retainer) {
        this.cache = createCache(context, retainer);
    }

    public ImageCache getCache() {
        return cache;
    }

    private ImageCache initCacheFromRetainFragment(Activity activity) {
        Retainer retainer =  activity instanceof  FragmentActivity ?
                SupportRetainFragment.findOrCreateRetainFragment(((FragmentActivity) activity).getSupportFragmentManager()) :
                RetainFragment.findOrCreateRetainFragment(activity.getFragmentManager());
        return createCache(activity, retainer);
    }

    private ImageCache createCache(Context context, Retainer retainer) {
        ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams(context, "images");
        params.diskCacheEnabled = true;
        params.setMemCacheSizePercent(0.25f);
        final ImageCache imageCache = ImageCache.getInstance(retainer, params);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                imageCache.initDiskCache();
            }
        });
        return imageCache;
    }

    public static CacheHandler getInstance(Activity activity) {
        return new CacheHandler(activity);
    }

    public static CacheHandler getInstance(Context context, Retainer retainer) {
        return new CacheHandler(context, retainer);
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
