package com.github.st1hy.sabre.image.surface;

import android.view.View;

import com.github.st1hy.sabre.R;

public class SurfaceViewDelegate {
    private final View root;
    private View loadingProgressBar;
    private ImageViewer viewer;

    public SurfaceViewDelegate(View root) {
        this.root = root;
    }

    public ImageViewer getViewer() {
        if (viewer == null) {
            viewer = (ImageViewer) root.findViewById(R.id.image_viewer_surface);
        }
        return viewer;
    }

    public View getLoadingProgressBar() {
        if (loadingProgressBar == null) {
            loadingProgressBar = root.findViewById(R.id.fragment_image_viewer_progress_bar);
        }
        return loadingProgressBar;
    }
}
