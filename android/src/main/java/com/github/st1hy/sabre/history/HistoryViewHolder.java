package com.github.st1hy.sabre.history;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.injector.ViewBinder;

import butterknife.Bind;

public class HistoryViewHolder extends ViewBinder {
    @Bind(R.id.main_activity_empty_view)
    View floatingButtonContainer;
    @Bind(R.id.text_open_image_help)
    View floatingButtonText;
    @Bind(R.id.open_image_button)
    View floatingButton;
    @Bind(R.id.history_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.empty_view)
    View emptyView;


    public View getFloatingButtonContainer() {
        return floatingButtonContainer;
    }

    public View getFloatingButtonText() {
        return floatingButtonText;
    }


    public View getFloatingButton() {
        return floatingButton;
    }


    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public View getEmptyView() {
        return emptyView;
    }
}
