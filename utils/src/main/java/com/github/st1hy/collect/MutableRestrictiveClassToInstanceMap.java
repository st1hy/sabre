package com.github.st1hy.collect;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.MutableClassToInstanceMap;

import java.util.Map;

public class MutableRestrictiveClassToInstanceMap extends ForwardingMap<Class<?>, Object> implements RestrictiveClassToInstanceMap {
    private final ClassToInstanceMap map;

    private MutableRestrictiveClassToInstanceMap(ClassToInstanceMap map) {
        this.map = map;
    }

    @Override
    protected Map<Class<?>, Object> delegate() {
        return null;
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
        MutableClassToInstanceMap instanceMap = MutableClassToInstanceMap.create();
        return new MutableRestrictiveClassToInstanceMap(instanceMap);
    }

    public static <B> RestrictiveClassToInstanceMap create(Map<Class<? extends B>, B> backingMap) {
        MutableClassToInstanceMap<B> instanceMap = MutableClassToInstanceMap.create(backingMap);
        return new MutableRestrictiveClassToInstanceMap(instanceMap);
    }
}
