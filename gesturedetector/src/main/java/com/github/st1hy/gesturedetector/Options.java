package com.github.st1hy.gesturedetector;

import android.content.Context;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gesture detection options.
 * <p/>
 * By default all {@link Options.Event events} are enabled and all {@link Flag flags} except {@link Flag#TRANSLATION_STRICT_ONE_FINGER} are set.
 */
public class Options implements Cloneable {
    private Set<Event> enabledEvents = EnumSet.allOf(Event.class);
    private Set<Flag> flags = EnumSet.allOf(Flag.class);
    private Map<Constant, Integer> constants = new ConcurrentHashMap<>();

    protected enum Event {
        SCALE, ROTATE, TRANSLATE, CLICK, DOUBLE_CLICK, LONG_PRESS, FLING
    }

    public enum Flag {
        /**
         * When detected other gestures don't trigger click event.
         */
        IGNORE_CLICK_EVENT_ON_GESTURES,
        /**
         * When multitouch gesture is detected use a middle point between fingers to calculate current translation.
         */
        TRANSLATION_MULTITOUCH,
        /**
         * Restrict translation to using one finger only. When multitouch is detected ends translations.
         * <p/>
         * If both {@link Flag#TRANSLATION_MULTITOUCH} and {@link Flag#TRANSLATION_STRICT_ONE_FINGER} are enabled {@link Flag#TRANSLATION_STRICT_ONE_FINGER} takes precedence.
         */
        TRANSLATION_STRICT_ONE_FINGER,
    }

    public enum Constant {
        LONG_PRESS_TIME_MS(500),
        DOUBLE_CLICK_TIME_LIMIT(400),
        TRANSLATION_START_THRESHOLD(100),;
        private final int defaultValue;

        Constant(int defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    public Options(Context context) {
        for (Constant constant : Constant.values()) {
            constants.put(constant, constant.defaultValue);
        }
        int translateStartThreshold = context.getResources().getDimensionPixelSize(R.dimen.gesture_detector_translation_start_threshold);
        constants.put(Constant.TRANSLATION_START_THRESHOLD, translateStartThreshold);

        flags.remove(Flag.TRANSLATION_STRICT_ONE_FINGER);
    }

    public boolean isEnabled(Event event, Event... events) {
        boolean isEnabled = enabledEvents.contains(event);
        if (events == null) return isEnabled;
        for (Event e : events) {
            isEnabled &= enabledEvents.contains(e);
            if (!isEnabled) return false;
        }
        return isEnabled;
    }

    public void setEnabled(Event event, boolean isEnabled) {
        if (isEnabled) {
            enabledEvents.add(event);
        } else {
            enabledEvents.remove(event);
        }
    }

    public boolean getFlag(Flag flag, Flag... flags) {
        boolean isSet = this.flags.contains(flag);
        if (flags == null) return isSet;
        for (Flag f : flags) {
            isSet &= this.flags.contains(f);
            if (!isSet) return false;
        }
        return isSet;
    }

    public void setFlag(Flag flag, boolean isEnabled) {
        if (isEnabled) {
            flags.add(flag);
        } else {
            flags.remove(flag);
        }
    }

    public int get(Constant constant) {
        return constants.get(constant);
    }

    public void set(Constant constant, int value) {
        constants.put(constant, value);
    }

    public Options clone() {
        try {
            Object object = super.clone();
            if (!object.getClass().equals(getClass()))
                throw new IllegalStateException("Clone not the same class");
            Options clone = getClass().cast(object);
            clone.enabledEvents = EnumSet.copyOf(enabledEvents);
            clone.flags = EnumSet.copyOf(flags);
            clone.constants = new ConcurrentHashMap<>(constants);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
