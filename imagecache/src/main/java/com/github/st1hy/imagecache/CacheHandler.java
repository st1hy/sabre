package com.github.st1hy.imagecache;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.github.st1hy.retainer.RetainFragment;
import com.github.st1hy.retainer.Retainer;
import com.github.st1hy.retainer.SupportRetainFragment;
import com.github.st1hy.core.utils.Utils;

import java.util.concurrent.Executor;

public class CacheHandler {
    private final ImageCache cache;
    private final Executor executor = Utils.CACHED_EXECUTOR_POOL;

    private CacheHandler(@NonNull Activity activity) {
        this.cache = initCacheFromRetainFragment(activity);
    }

    private CacheHandler(@NonNull Context context, @NonNull Retainer retainer) {
        this.cache = createCache(context, retainer);
    }

    @NonNull
    public ImageCache getCache() {
        return cache;
    }

    @NonNull
    private ImageCache initCacheFromRetainFragment(@NonNull Activity activity) {
        Retainer retainer = activity instanceof FragmentActivity ?
                SupportRetainFragment.findOrCreateRetainFragment(((FragmentActivity) activity).getSupportFragmentManager()) :
                RetainFragment.findOrCreateRetainFragment(activity.getFragmentManager());
        return createCache(activity, retainer);
    }

    @NonNull
    private ImageCache createCache(@NonNull Context context, @NonNull Retainer retainer) {
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

    @NonNull
    public static CacheHandler getInstance(@NonNull Activity activity) {
        return new CacheHandler(activity);
    }

    @NonNull
    public static CacheHandler getInstance(@NonNull Context context, @NonNull Retainer retainer) {
        return new CacheHandler(context, retainer);
    }

    public void onStop() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                cache.flush();
            }
        });
    }

    public void onDestroy() {
        cache.close();
    }
}
