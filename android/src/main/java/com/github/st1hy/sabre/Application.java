package com.github.st1hy.sabre;

import com.github.st1hy.gesturedetector.Config;
import com.github.st1hy.sabre.core.cache.retainer.ObjectRetainer;
import com.github.st1hy.sabre.core.cache.retainer.Retainer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Application extends android.app.Application implements Retainer {
    private final ObjectRetainer retainer = new ObjectRetainer();

    @Override
    public void onCreate() {
        super.onCreate();
        Config.DEBUG = BuildConfig.DEBUG;
    }

    @Override
    public Object get(String key) {
        return retainer.get(key);
    }

    @Override
    public void put(String key, Object value) {
        retainer.put(key, value);
    }

    public static final Executor CACHED_EXECUTOR_POOL = Executors.newCachedThreadPool();

}
