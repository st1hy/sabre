package com.github.st1hy.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.Map;

/**
 * Selects {@link android.view.View.OnTouchListener} based on {@link TouchPredicate}.
 * <p>
 * Selector selects listener when touch event is started. Selector iterates over order and selects
 * first item that pass condition. All events goes to single output until next start of the touch event.
 */
public class SelectorOnTouchListener<T extends View.OnTouchListener> implements View.OnTouchListener {
    private final Map<TouchPredicate, T> conditionMap;
    private final List<TouchPredicate> order;
    private View.OnTouchListener forwardTo;

    public SelectorOnTouchListener(@NonNull Map<TouchPredicate, T> conditionMap,
                                   @NonNull List<TouchPredicate> order) {
        this.conditionMap = conditionMap;
        this.order = order;
    }

    @NonNull
    public List<TouchPredicate> getOrder() {
        return order;
    }

    @NonNull
    public Map<TouchPredicate, T> getConditionMap() {
        return conditionMap;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            forwardTo = selectListener(v, event);
        }
        return forwardTo != null && forwardTo.onTouch(v, event);
    }

    @Nullable
    private T selectListener(@NonNull View v, @NonNull MotionEvent event) {
        for (TouchPredicate predicate : order) {
            if (predicate.canForward(v, event)) {
                return conditionMap.get(predicate);
            }
        }
        return null;
    }

}
