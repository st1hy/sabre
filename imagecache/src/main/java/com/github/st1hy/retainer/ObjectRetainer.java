package com.github.st1hy.retainer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ObjectRetainer implements Retainer {
    private final Map<String,Object> map = Collections.synchronizedMap(new HashMap<String, Object>());

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
