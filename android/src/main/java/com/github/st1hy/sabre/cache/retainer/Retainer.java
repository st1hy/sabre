package com.github.st1hy.sabre.cache.retainer;

public interface Retainer {
    Object get(String key);

    void put(String key, Object value);
}
