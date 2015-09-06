package com.github.st1hy.sabre.history;

import android.view.View;

import com.github.st1hy.sabre.R;

public class HistoryViewDelegate {
    private final View root;
    private View floatingButtonContainer;
    private View floatingButtonText;

    public HistoryViewDelegate(View root) {
        this.root = root;
    }

    public View getFloatingButtonContainer() {
        if (floatingButtonContainer == null) {
            floatingButtonContainer = root.findViewById(R.id.main_activity_empty_view);
        }
        return floatingButtonContainer;
    }

    public View getFloatingButtonText() {
        if (floatingButtonText == null) {
            floatingButtonText = getFloatingButtonContainer().findViewById(R.id.text_open_image_help);
        }
        return floatingButtonText;
    }
}
