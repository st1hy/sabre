package com.github.st1hy.sabre;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.github.st1hy.sabre.util.SystemUIMode;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE = 0x16ed;
    private static final String SAVE_IMAGE_URI = "com.github.st1hy.sabre.IMAGE_URI";
    private Uri loadedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            loadedImage = savedInstanceState.getParcelable(SAVE_IMAGE_URI);
            onImageLoaded(loadedImage);
        } else {
            switchUIMode(SystemUIMode.DEFAULT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onActionOpen(final MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    public void onActionSettings(final MenuItem item) {
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
        ImageView imageView = (ImageView) findViewById(R.id.main_activity_background);
        imageView.setImageURI(loadedImage);
        switchUIMode(SystemUIMode.IMMERSIVE);
    }
}
