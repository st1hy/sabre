package com.github.st1hy.sabre.history;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.st1hy.sabre.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HistoryViewHolder {
    @Bind(R.id.history_fab)
    View floatingButtonContainer;
    @Bind(R.id.text_open_image_help)
    View floatingButtonText;
    @Bind(R.id.open_image_button)
    View floatingButton;
    @Bind(R.id.history_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.history_empty_text_view)
    View emptyView;
    @Bind(R.id.history_toolbar)
    Toolbar toolbar;

    public void bind(@NonNull Activity activity) {
        ButterKnife.bind(this, activity);
    }

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

    public Toolbar getToolbar() {
        return toolbar;
    }
}
