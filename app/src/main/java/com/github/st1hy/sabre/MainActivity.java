package com.github.st1hy.sabre;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.github.st1hy.sabre.image.ImageViewer;
import com.github.st1hy.sabre.util.SystemUIMode;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE = 0x16ed;
    private static final String SAVE_IMAGE_URI = "com.github.st1hy.sabre.IMAGE_URI";
    private Uri loadedImage;
    private ViewDelegate viewDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewDelegate = new ViewDelegate(this);
        if (savedInstanceState != null) {
            loadedImage = savedInstanceState.getParcelable(SAVE_IMAGE_URI);
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
        ImageViewer viewer = (ImageViewer) findViewById(R.id.main_activity_image_viewer);
        viewer.setImageURI(loadedImage);
        if (loadedImage == null) return;
        switchUIMode(SystemUIMode.IMMERSIVE);
    }
}
