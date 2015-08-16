package com.github.st1hy.gesturedetector;

import android.content.Context;
import android.content.res.Resources;

import java.util.EnumMap;
import java.util.EnumSet;

import static com.github.st1hy.gesturedetector.Options.Constant.DOUBLE_CLICK_TIME_LIMIT;
import static com.github.st1hy.gesturedetector.Options.Constant.LONG_PRESS_TIME_MS;
import static com.github.st1hy.gesturedetector.Options.Constant.SCALE_START_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Constant.TRANSLATION_START_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Flag.TRANSLATION_STRICT_ONE_FINGER;

/**
 * Gesture detection options.
 * <p/>
 * By default all {@link Options.Event events} are enabled and all {@link Flag flags} except {@link Flag#TRANSLATION_STRICT_ONE_FINGER} are set.
 */
public class Options implements Cloneable {
    private EnumSet<Event> enabledEvents = EnumSet.allOf(Event.class);
    private EnumSet<Flag> flags = EnumSet.allOf(Flag.class);
    private EnumMap<Constant, Integer> constants = new EnumMap<>(Constant.class);

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
         * Restrict translation to using one finger only. When multitouch is detected end translations.
         * <p/>
         * If both {@link Flag#TRANSLATION_MULTITOUCH} and {@link Flag#TRANSLATION_STRICT_ONE_FINGER} are enabled {@link Flag#TRANSLATION_STRICT_ONE_FINGER} takes precedence.
         */
        TRANSLATION_STRICT_ONE_FINGER,
    }

    public enum Constant {
        LONG_PRESS_TIME_MS(500),
        DOUBLE_CLICK_TIME_LIMIT(400),
        TRANSLATION_START_THRESHOLD(100),
        SCALE_START_THRESHOLD(100),;
        private final int defaultValue;

        Constant(int defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    public Options(Context context) {
        for (Constant constant : Constant.values()) {
            constants.put(constant, constant.defaultValue);
        }
        Resources resources = context.getResources();
        constants.put(LONG_PRESS_TIME_MS, resources.getInteger(R.integer.gesture_detector_long_press_time));
        constants.put(DOUBLE_CLICK_TIME_LIMIT, resources.getInteger(R.integer.gesture_detector_double_click_time_limit));
        int translateStartThreshold = resources.getDimensionPixelSize(R.dimen.gesture_detector_translation_start_threshold);
        constants.put(TRANSLATION_START_THRESHOLD, translateStartThreshold);
        int scaleStartThreshold = resources.getDimensionPixelSize(R.dimen.gesture_detector_translation_start_threshold);
        constants.put(SCALE_START_THRESHOLD, scaleStartThreshold);

        flags.remove(TRANSLATION_STRICT_ONE_FINGER);
        enabledEvents.remove(Event.TRANSLATE);
    }

    public boolean isEnabled(Event event, Event... events) {
        boolean isEnabled = enabledEvents.contains(event);
        if (events == null || !isEnabled) return isEnabled;
        for (Event e : events) {
            if (!enabledEvents.contains(e)) return false;
        }
        return true;
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
        if (flags == null || !isSet) return isSet;
        for (Flag f : flags) {
            if (!this.flags.contains(f)) return false;
        }
        return true;
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
            clone.constants = new EnumMap<>(constants);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
