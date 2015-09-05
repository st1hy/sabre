package com.github.st1hy.sabre.gdx;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.github.st1hy.sabre.R;

public class ViewDelegate {
    private final Activity activity;
    private ViewGroup viewerContainer;
    private View emptyView;
    private View loadingProgressBar;

    public ViewDelegate(Activity activity) {
        this.activity = activity;
    }

    public ViewGroup getViewerContainer() {
        if (viewerContainer == null) {
            viewerContainer = (ViewGroup) activity.findViewById(R.id.main_activity_image_viewer);
        }
        return viewerContainer;
    }

    public View getLoadingProgressBar() {
        if (loadingProgressBar == null) {
            loadingProgressBar = activity.findViewById(R.id.main_activity_progress_bar);
        }
        return loadingProgressBar;
    }

    public View getEmptyView() {
        if (emptyView == null) {
            emptyView = activity.findViewById(R.id.main_activity_empty_view);
        }
        return emptyView;
    }
}
