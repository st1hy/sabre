package com.github.st1hy.collect;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.MutableClassToInstanceMap;

import java.util.Map;

public class MutableRestrictiveClassToInstanceMap extends ForwardingMap<Class<?>, Object> implements RestrictiveClassToInstanceMap {
    private final ClassToInstanceMap<Object> map;

    private MutableRestrictiveClassToInstanceMap(ClassToInstanceMap<Object> map) {
        this.map = map;
    }

    @Override
    protected Map<Class<?>, Object> delegate() {
        return map;
    }

    @Override
    public <T> T getInstance(@NonNull Class<T> type) {
        return type.cast(map.getInstance(type));
    }

    @Override
    public <T> T putInstance(@NonNull Class<T> type, @Nullable T value) {
        T old = getInstance(type);
        map.putInstance(type, value);
        return old;
    }

    public static RestrictiveClassToInstanceMap create() {
        MutableClassToInstanceMap<Object> instanceMap = MutableClassToInstanceMap.create();
        return new MutableRestrictiveClassToInstanceMap(instanceMap);
    }

    public static RestrictiveClassToInstanceMap create(Map<Class<?>, Object> backingMap) {
        MutableClassToInstanceMap<Object> instanceMap = MutableClassToInstanceMap.create(backingMap);
        return new MutableRestrictiveClassToInstanceMap(instanceMap);
    }
}
