package com.github.st1hy.sabre.surface;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.cache.ImageCache;
import com.github.st1hy.sabre.surface.image.ImageViewer;
import com.github.st1hy.sabre.surface.image.ImageSurfaceViewer;
import com.github.st1hy.sabre.util.SystemUIMode;

public class MainActivity extends AppCompatActivity implements ImageSurfaceViewer.ImageLoadingCallback {
    private static final int REQUEST_IMAGE = 0x16ed;
    private static final String SAVE_IMAGE_URI = "com.github.st1hy.sabre.IMAGE_URI";
    private Uri loadedImage;
    private ViewDelegate viewDelegate;
    private ImageCache imageCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            loadedImage = savedInstanceState.getParcelable(SAVE_IMAGE_URI);
            if (loadedImage != null) switchUIMode(SystemUIMode.IMMERSIVE);
        }
        initCache();
        setContentView(R.layout.activity_main);
        viewDelegate = new ViewDelegate(this);
        viewDelegate.getViewer().setLoadingCallback(this);
        viewDelegate.getViewer().addImageCache(imageCache);
        if (loadedImage != null) {
            onImageLoaded(loadedImage);
        }
    }

    private ImageCache initCache() {
        ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams(this, "images");
        params.diskCacheEnabled = false;
        params.setMemCacheSizePercent(0.25f);
        imageCache = ImageCache.getInstance(getFragmentManager(), params);
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                imageCache.initDiskCache();
            }
        });
        return imageCache;
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
        ImageViewer viewer = viewDelegate.getViewer();
        viewer.setImageURI(loadedImage);
    }

    @Override
    public void onImageLoadingStarted() {
        viewDelegate.getEmptyView().setVisibility(View.GONE);
        viewDelegate.getLoadingProgressBar().setVisibility(View.VISIBLE);
    }

    @Override
    public void onImageLoadingFinished() {
        viewDelegate.getLoadingProgressBar().setVisibility(View.GONE);
        viewDelegate.getViewer().setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewDelegate.getViewer().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewDelegate.getViewer().onPause();
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                imageCache.flush();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageCache.close();
    }
}
