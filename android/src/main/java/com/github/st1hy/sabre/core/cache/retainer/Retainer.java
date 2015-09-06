package com.github.st1hy.sabre.core.cache.retainer;

public interface Retainer {
    Object get(String key);

    void put(String key, Object value);
}
