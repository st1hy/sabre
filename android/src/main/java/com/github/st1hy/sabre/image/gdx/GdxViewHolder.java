package com.github.st1hy.sabre.image.gdx;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.injector.ViewBinder;

import butterknife.Bind;

public class GdxViewHolder extends ViewBinder {
    private View glSurface;

    @Bind(R.id.image_viewer_gl_surface_container)
    ViewGroup glSurfaceContainer;
    @Bind(R.id.fragment_image_viewer_progress_bar)
    View loadingProgressBar;

    public GdxViewHolder(@NonNull  View glSurface) {
        this.glSurface = glSurface;
    }

    @Override
    public GdxViewHolder bind(@NonNull View view) {
        super.bind(view);
        glSurfaceContainer.addView(glSurface);
        return this;
    }

    @Override
    public ViewBinder unbind() {
        glSurface = null;
        return super.unbind();
    }

    public View getLoadingProgressBar() {
        return loadingProgressBar;
    }

    public ViewGroup getGlSurfaceContainer() {
        return glSurfaceContainer;
    }

    public View getGlSurface() {
        return glSurface;
    }
}
