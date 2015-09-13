package com.github.st1hy.sabre.history;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.st1hy.sabre.R;

public class HistoryViewDelegate {
    private final View root;
    private View floatingButtonContainer;
    private View floatingButtonText;
    private RecyclerView recyclerView;
    private View emptyView;

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

    public RecyclerView getRecyclerView() {
        if (recyclerView == null) {
            recyclerView = (RecyclerView) root.findViewById(R.id.history_recycler_view);
        }
        return recyclerView;
    }

    public View getEmptyView() {
        if (emptyView == null) {
            emptyView = root.findViewById(R.id.empty_view);
        }
        return emptyView;
    }
}
