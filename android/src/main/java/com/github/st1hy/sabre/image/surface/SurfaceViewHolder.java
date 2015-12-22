package com.github.st1hy.sabre.image.surface;

import android.view.View;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.injector.ViewBinder;

import butterknife.Bind;

public class SurfaceViewHolder extends ViewBinder {
    @Bind(R.id.fragment_image_viewer_progress_bar)
    View loadingProgressBar;
    @Bind(R.id.image_viewer_surface)
    ImageViewer viewer;

    @Override
    public ViewBinder unbind() {
        getViewer().onDestroy();
        return super.unbind();
    }

    public ImageViewer getViewer() {
        return viewer;
    }

    public View getLoadingProgressBar() {
        return loadingProgressBar;
    }
}
