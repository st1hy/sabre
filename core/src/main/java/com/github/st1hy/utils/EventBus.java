package com.github.st1hy.utils;

import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public class EventBus {
    public static final EventBus INSTANCE = new EventBus();
    private final Map<Class, Array> map = new HashMap<>();


    private EventBus() {
    }

    public synchronized <T> void add(Class<T> tag, T object) {
        Array<T> objects = get(tag);
        if (objects == null) {
            objects = new Array<>();
            map.put(tag, objects);
        }
        objects.add(object);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> Array<T> get(Class<T> tag) {
        return (Array<T>) map.get(tag);
    }

    public synchronized <T> void remove(Class<T> tag, T object) {
        Array<T> objects = get(tag);
        if (objects == null) return;
        objects.removeValue(object, true);
        if (objects.size == 0) map.remove(tag);
    }

    public synchronized <T> void removeAll(Class<T> tag) {
        map.remove(tag);
    }

    public synchronized <T> void apply(Class<T> tag, EventMethod<T> function) {
        Iterable<T> collection = get(tag);
        if (collection != null)
            for (T t : collection) {
                function.apply(t);
            }
    }

}