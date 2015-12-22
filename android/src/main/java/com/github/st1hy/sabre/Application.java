package com.github.st1hy.sabre;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.retainer.ObjectRetainer;
import com.github.st1hy.retainer.Retainer;
import com.github.st1hy.gesturedetector.Config;
import com.github.st1hy.core.utils.Utils;

import java.util.concurrent.Executor;

import timber.log.Timber;

public class Application extends android.app.Application implements Retainer {
    private final ObjectRetainer retainer = new ObjectRetainer();

    public static final Executor CACHED_EXECUTOR_POOL = Utils.CACHED_EXECUTOR_POOL;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Config.DEBUG = BuildConfig.DEBUG;
    }

    @Override
    @Nullable
    public Object get(@NonNull String key) {
        return retainer.get(key);
    }

    @Override
    public void put(@NonNull String key, @Nullable Object value) {
        retainer.put(key, value);
    }
}
