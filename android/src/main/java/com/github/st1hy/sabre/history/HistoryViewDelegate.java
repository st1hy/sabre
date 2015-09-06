package com.github.st1hy.sabre.history;

import android.view.View;
import android.widget.ListView;

import com.github.st1hy.sabre.R;

public class HistoryViewDelegate {
    private final View root;
    private View floatingButtonContainer;
    private View floatingButtonText;
    private ListView listView;
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

    public ListView getListView() {
        if (listView == null) {
            listView = (ListView) root.findViewById(R.id.history_list_view);
        }
        return listView;
    }

    public View getEmptyView() {
        if (emptyView == null) {
            emptyView = root.findViewById(R.id.empty_view);
        }
        return emptyView;
    }
}
