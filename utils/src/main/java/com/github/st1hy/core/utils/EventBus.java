package com.github.st1hy.core.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
    public static final EventBus INSTANCE = new EventBus();
    private Multimap<Class, Object> map = Multimaps.synchronizedMultimap(Multimaps.newMultimap(
            new LinkedHashMap<Class, Collection<Object>>(),
            new Supplier<Collection<Object>>() {
                @Override
                public Collection<Object> get() {
                    return new CopyOnWriteArrayList<>();
                }
            }));

    private EventBus() {
    }

    public <T> void add(@NonNull Class<T> tag, @NonNull T object) {
        map.put(tag, object);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Collection<T> get(@Nullable Class<T> tag) {
        return (Collection<T>) map.get(tag);
    }

    public <T> void remove(@NonNull Class<T> tag, @NonNull T object) {
        map.remove(tag, object);
    }

    public <T> void removeAll(@NonNull Class<T> tag) {
        map.removeAll(tag);
    }

    public <T> void apply(@NonNull Class<T> tag, @NonNull Function<T, Void> function) {
        Collection<T> collection = get(tag);
        if (collection != null && !collection.isEmpty())
            for (T t : collection) {
                function.apply(t);
            }

    }
}
