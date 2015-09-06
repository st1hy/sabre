package com.github.st1hy.sabre.image.surface;

import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.github.st1hy.sabre.MainActivity;
import com.github.st1hy.sabre.NavState;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.cache.ImageCache;
import com.github.st1hy.sabre.core.util.SystemUIMode;

public class SurfaceImageViewFragment extends Fragment implements ImageViewer.ImageLoadingCallback {
    private SurfaceViewDelegate viewDelegate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_image_surface, container, false);
        viewDelegate = new SurfaceViewDelegate(root);
        viewDelegate.getViewer().setLoadingCallback(this);
        ImageCache imageCache = ((MainActivity) getActivity()).getDependencyDelegate().getCacheHandler().getCache();
        viewDelegate.getViewer().addImageCache(imageCache);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            Parcelable parcelable = arguments.getParcelable(NavState.ARG_IMAGE_URI);
            if (parcelable instanceof Uri) {
                setImageURI((Uri) parcelable);
            }
        }
    }

    private void setImageURI(Uri loadedImage) {
        ImageViewer viewer = viewDelegate.getViewer();
        viewer.setImageURI(loadedImage);
    }

    @Override
    public void onImageLoadingStarted() {
        viewDelegate.getViewer().setVisibility(View.INVISIBLE);
        viewDelegate.getLoadingProgressBar().setVisibility(View.VISIBLE);
    }

    @Override
    public void onImageLoadingFinished() {
        viewDelegate.getViewer().setVisibility(View.VISIBLE);
        viewDelegate.getLoadingProgressBar().setVisibility(View.GONE);
        Window window = getActivity().getWindow();
        SystemUIMode.IMMERSIVE.apply(window);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    @Override
    public void onResume() {
        super.onResume();
        viewDelegate.getViewer().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewDelegate.getViewer().onPause();
    }

}
