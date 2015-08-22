package com.github.st1hy.gesturedetector;

import android.content.res.Resources;

import java.util.EnumMap;
import java.util.EnumSet;

import static com.github.st1hy.gesturedetector.Options.Constant.DOUBLE_CLICK_TIME_LIMIT;
import static com.github.st1hy.gesturedetector.Options.Constant.FLING_TRANSLATION_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Constant.FLING_VELOCITY_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Constant.LONG_PRESS_TIME_MS;
import static com.github.st1hy.gesturedetector.Options.Constant.ROTATION_START_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Constant.SCALE_START_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Constant.TRANSLATION_START_THRESHOLD;
import static com.github.st1hy.gesturedetector.Options.Flag.TRANSLATION_STRICT_ONE_FINGER;

/**
 * Gesture detection options.
 * <p/>
 * By default all {@link Options.Event events} are enabled and all {@link Flag flags} except {@link Flag#TRANSLATION_STRICT_ONE_FINGER} are set.
 */
public class Options implements Cloneable {
    private EnumSet<Event> enabledEvents = EnumSet.noneOf(Event.class);
    private EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
    private EnumMap<Constant, Integer> constants = new EnumMap<>(Constant.class);

    public enum Event {
        SCALE, ROTATE, TRANSLATE, CLICK, DOUBLE_CLICK, LONG_PRESS, FLING
    }

    public enum Flag {
        /**
         * When detected other gestures don't trigger click event.
         */
        IGNORE_CLICK_EVENT_ON_GESTURES,
        /**
         * Restrict translation to using one finger only. When multitouch is detected end translations.
         */
        TRANSLATION_STRICT_ONE_FINGER,
    }

    public enum Constant {
        /**
         * Hardcoded default: 500 ms
         */
        LONG_PRESS_TIME_MS(500),
        /**
         * Hardcoded default: 400 ms
         */
        DOUBLE_CLICK_TIME_LIMIT(400),
        /**
         * Hardcoded default: 20 px
         */
        TRANSLATION_START_THRESHOLD(20),
        /**
         * Hardcoded default: 20 px
         */
        ROTATION_START_THRESHOLD(20),
        /**
         * Hardcoded default: 20 px
         */
        SCALE_START_THRESHOLD(20),
        /**
         * Hardcoded default: 100 px / s
         */
        FLING_VELOCITY_THRESHOLD(100),
        /**
         * How much pointer have to move to consider this a fling.
         *
         * i.e value 50 means that pointer needs to move 50 % of the related view width or height depending on the fling.
         * Hardcoded default: 50 %
         */
        FLING_TRANSLATION_THRESHOLD(50);
        private final int defaultValue;

        Constant(int defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    /**
     * Creates default Options using provided {@link Resources}.
     *
     * @param resources Resources providing default values.
     * @throws NullPointerException if resources are null.
     */
    public Options(Resources resources) {
        if (resources == null) throw new NullPointerException("Resources cannot be null");
        enabledEvents.addAll(EnumSet.allOf(Event.class));
        flags.addAll(EnumSet.allOf(Flag.class));
        flags.remove(TRANSLATION_STRICT_ONE_FINGER);

        for (Constant constant : Constant.values()) {
            constants.put(constant, constant.defaultValue);
        }
        constants.put(LONG_PRESS_TIME_MS, resources.getInteger(R.integer.gesture_detector_long_press_time));
        constants.put(DOUBLE_CLICK_TIME_LIMIT, resources.getInteger(R.integer.gesture_detector_double_click_time_limit));
        constants.put(TRANSLATION_START_THRESHOLD, resources.getDimensionPixelSize(R.dimen.gesture_detector_translation_start_threshold));
        constants.put(SCALE_START_THRESHOLD, resources.getDimensionPixelSize(R.dimen.gesture_detector_translation_start_threshold));
        constants.put(ROTATION_START_THRESHOLD, resources.getDimensionPixelSize(R.dimen.gesture_detector_rotation_start_threshold));
        constants.put(FLING_VELOCITY_THRESHOLD, resources.getDimensionPixelSize(R.dimen.gesture_detector_fling_velocity_threshold));
        constants.put(FLING_TRANSLATION_THRESHOLD, resources.getInteger(R.integer.gesture_detector_fling_translation_threshold));
    }

    /**
     * Creates empty options.
     * <p/>
     * None of the {@link Event events} or {@link Flag flags} are set. Constants will return 0.
     */
    public Options() {
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
        Integer value = constants.get(constant);
        return value != null ? value : 0;
    }

    public void set(Constant constant, int value) {
        constants.put(constant, value);
    }

    public Options clone() {
        try {
            Object object = super.clone();
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
