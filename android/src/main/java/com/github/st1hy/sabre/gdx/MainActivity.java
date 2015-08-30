package com.github.st1hy.sabre.gdx;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.gdx.image.ImageViewer;
import com.github.st1hy.sabre.gdx.image.ImageViewerAdapter;
import com.github.st1hy.sabre.util.SystemUIMode;

public class MainActivity extends AndroidApplication implements ImageViewer.ImageLoadingCallback {
    private static final int REQUEST_IMAGE = 0x16ed;
    private static final String SAVE_IMAGE_URI = "com.github.st1hy.sabre.IMAGE_URI";
    private Uri loadedImage;
    private ViewDelegate viewDelegate;
    private ImageViewer imageViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            loadedImage = savedInstanceState.getParcelable(SAVE_IMAGE_URI);
        }
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main_gdx);
        viewDelegate = new ViewDelegate(this);
        imageViewer = new ImageViewerAdapter(this, viewDelegate.getViewerContainer());
        imageViewer.setLoadingCallback(this);
        if (loadedImage != null) {
            onImageLoaded(loadedImage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Called using menu_main.xml
    public void onActionOpen(final MenuItem item) {
        onActionOpen();
    }

    //Called using menu_main.xml
    public void onActionSettings(final MenuItem item) {
        //TODO Add settings or remove;
    }

    //Called using main_empty_view.xml
    public void onActionOpen(final View view) {
        onActionOpen();
    }

    private void onActionOpen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE:
                if (resultCode == RESULT_OK && null != data) {
                    loadedImage = data.getData();
                    onImageLoaded(loadedImage);
                    switchUIMode(SystemUIMode.IMMERSIVE);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void switchUIMode(SystemUIMode option) {
        option.apply(getWindow());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (loadedImage != null) outState.putParcelable(SAVE_IMAGE_URI, loadedImage);
    }

    private void onImageLoaded(Uri loadedImage) {
        imageViewer.setImageURI(loadedImage);
    }

    @Override
    public void onImageLoadingStarted() {
        viewDelegate.getEmptyView().setVisibility(View.GONE);
        viewDelegate.getLoadingProgressBar().setVisibility(View.VISIBLE);
    }

    @Override
    public void onImageLoadingFinished() {
        viewDelegate.getLoadingProgressBar().setVisibility(View.GONE);
//        imageViewer.setVisibility(View.VISIBLE);
    }

}
