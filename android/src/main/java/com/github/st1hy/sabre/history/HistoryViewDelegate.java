package com.github.st1hy.sabre.history;

import android.view.View;

import com.github.st1hy.sabre.R;

public class HistoryViewDelegate {
    private final View root;
    private View floatingButtonContainer;

    public HistoryViewDelegate(View root) {
        this.root = root;
    }

    public View getFloatingButtonContainer() {
        if (floatingButtonContainer == null) {
            floatingButtonContainer = root.findViewById(R.id.main_activity_empty_view);
        }
        return floatingButtonContainer;
    }
}
