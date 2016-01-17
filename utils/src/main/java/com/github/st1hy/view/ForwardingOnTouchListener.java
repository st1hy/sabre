package com.github.st1hy.view;

import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Utility class for forwarding touch events and checking set of preconditions.
 *
 * If any of precondition is false event is not forwarded.
 */
public class ForwardingOnTouchListener<T extends View.OnTouchListener> implements View.OnTouchListener {
    private final T delegate;
    private final List<TouchPredicate> whenNotForward;

    public ForwardingOnTouchListener(T onTouchListener) {
        this.delegate = onTouchListener;
        whenNotForward = Lists.newArrayList();
    }

    public ForwardingOnTouchListener(T delegate, List<TouchPredicate> whenNotForward) {
        this.delegate = delegate;
        this.whenNotForward = Lists.newArrayList(whenNotForward);
    }

    protected T delegate() {
        return delegate;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return checkIfForwardingPossible(v, event) && delegate.onTouch(v, event);
    }

    private boolean checkIfForwardingPossible(View v, MotionEvent event) {
        boolean canForward = true;
        for (TouchPredicate precondition : whenNotForward) {
            canForward = precondition.canForward(v, event);
            if (!canForward) break;

        }
        return canForward;
    }

    public interface TouchPredicate {
        boolean canForward(View v, MotionEvent event);
    }
}
