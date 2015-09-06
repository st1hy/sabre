package com.github.st1hy.sabre.core.cache.retainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ObjectRetainer implements Retainer {
    private final Map<String,Object> map = Collections.synchronizedMap(new HashMap<String, Object>());

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    @Override
    public void put(String key, Object value) {
        map.put(key, value);
    }
}
