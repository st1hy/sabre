package com.github.st1hy.retainer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectRetainer implements Retainer {
    private final Map<String,Object> map = new ConcurrentHashMap<>();

    @Override
    @Nullable
    public Object get(@NonNull String key) {
        return map.get(key);
    }

    @Override
    public void put(@NonNull String key, @Nullable Object value) {
        map.put(key, value);
    }
}
