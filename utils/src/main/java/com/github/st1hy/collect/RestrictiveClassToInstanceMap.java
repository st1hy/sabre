package com.github.st1hy.collect;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * Restrictive fork of {@link com.google.common.collect.ClassToInstanceMap}. This version performs
 * static type check instead of dynamic when storing and getting instances of classes .
 */
public interface RestrictiveClassToInstanceMap extends Map<Class<?>, Object> {

    @Nullable
    <T> T getInstance(@NonNull Class<T> type);

    <T> T putInstance(@NonNull Class<T> type, @Nullable T value);

}
