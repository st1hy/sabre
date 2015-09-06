package com.github.st1hy.sabre.image.gdx;

import android.view.View;
import android.view.ViewGroup;

import com.github.st1hy.sabre.R;

public class GdxViewDelegate {
    private final View root;
    private final View glSurface;
    private final ViewGroup glSurfaceContainer;
    private View loadingProgressBar;
    public GdxViewDelegate(View root, View glSurface) {
        this.root = root;
        this.glSurface = glSurface;
        glSurfaceContainer = (ViewGroup) root.findViewById(R.id.image_viewer_gl_surface_container);
        glSurfaceContainer.addView(glSurface);

    }

    public View getLoadingProgressBar() {
        if (loadingProgressBar == null) {
            loadingProgressBar = root.findViewById(R.id.fragment_image_viewer_progress_bar);
        }
        return loadingProgressBar;
    }

    public ViewGroup getGlSurfaceContainer() {
        return glSurfaceContainer;
    }

    public View getGlSurface() {
        return glSurface;
    }
}
