package com.github.st1hy.sabre;

import android.view.View;

import com.github.st1hy.sabre.image.ImageViewer;

public class ViewDelegate {
    private final MainActivity activity;
    private View emptyView;
    private View loadingProgressBar;
    private ImageViewer viewer;

    public ViewDelegate(MainActivity activity) {
        this.activity = activity;
    }

    public ImageViewer getViewer() {
        if (viewer == null) {
            viewer = (ImageViewer) activity.findViewById(R.id.main_activity_image_viewer);
        }
        return viewer;
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
