package com.github.st1hy.sabre.history;

import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.st1hy.dao.DaoMaster;
import com.github.st1hy.dao.OpenedImageContentProvider;
import com.github.st1hy.imagecache.ImageCacheHandler;
import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.CacheUtils;
import com.github.st1hy.sabre.image.ImageActivity;
import com.github.st1hy.sabre.settings.SettingsActivity;
import com.google.common.base.Preconditions;

public class HistoryActivity extends AppCompatActivity implements HistoryRecyclerAdapter.OnImageClicked, ComponentCallbacks2 {
    private static final int REQUEST_IMAGE = 0x16ed;
    private static final String SAVE_ANIMATION_SHOW_FLAG = "animation shown";

    private final HistoryViewHolder viewHolder = new HistoryViewHolder();
    private ImageCacheHandler imageCacheHandler;
    private boolean showHelp = true;
    private HistoryRecyclerAdapter historyAdapter;
    private Animation fade_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageCacheHandler = CacheUtils.newImageCacheHandler((Application) getApplication());
        fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        showHelp = needShowHelp(savedInstanceState);
        historyAdapter = new HistoryRecyclerAdapter(this, this, imageCacheHandler.getCache());
        setupDao();
        SettingsActivity.loadDefaultSettings(this, false);
        getApplication().registerComponentCallbacks(this);

        setContentView(R.layout.activity_history);
        bind();
        setSupportActionBar(viewHolder.getToolbar());
    }

    private boolean needShowHelp(Bundle savedInstanceState) {
        return savedInstanceState == null || savedInstanceState.getBoolean(SAVE_ANIMATION_SHOW_FLAG, true);
    }

    private void setupDao() {
        Application app = (Application) getApplication();
        DaoMaster daoMaster = app.getCache().getInstance(DaoMaster.class);
        Preconditions.checkNotNull(daoMaster);
        OpenedImageContentProvider.daoSession = daoMaster.newSession();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplication().unregisterComponentCallbacks(this);
        imageCacheHandler.onStop();
        imageCacheHandler.onDestroy();
        historyAdapter.onDestroy();
        OpenedImageContentProvider.daoSession = null;
    }

    private void bind() {
        viewHolder.bind(this);
        viewHolder.getRecyclerView().setAdapter(historyAdapter);
        viewHolder.getRecyclerView().setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        viewHolder.getFloatingButtonText().setVisibility(View.GONE);
        getSupportLoaderManager().initLoader(0, null, new LoaderCallback());
        viewHolder.getFloatingButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionOpen();
            }
        });
    }

    private class LoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return historyAdapter.onCreateLoader(id, args);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            onDataLoaded(data);
            historyAdapter.onLoadFinished(loader, data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            historyAdapter.onLoaderReset(loader);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_ANIMATION_SHOW_FLAG, showHelp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    public void onActionOpen(final MenuItem item) {
        onActionOpen();
    }

    private void onActionOpen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE);
    }


    //Called using menu_history.xml
    public void onActionSettings(final MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE:
                if (resultCode == Activity.RESULT_OK && null != data) {
                    Uri uri = data.getData();
                    openImage(uri);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void openImage(@NonNull Uri uri) {
        Intent intent = new Intent();
        intent.setDataAndTypeAndNormalize(uri, "image/*");
        intent.setAction(Intent.ACTION_VIEW);
        intent.setClass(this, ImageActivity.class);
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            startActivityWithExitTransition(intent);
//        } else {
//            startActivity(intent);
//        }
        startActivity(intent);
    }

//    @TargetApi(21)
//    private void startActivityWithExitTransition(Intent intent) {
//        Bundle bundle = ActivityOptions
//                .makeSceneTransitionAnimation(getActivity())
//                .toBundle();
//        startActivity(intent, bundle);
//    }

    private void onDataLoaded(Cursor data) {
        if (data.getCount() > 0) {
            showHelp = false;
            viewHolder.getEmptyView().setVisibility(View.GONE);
        } else if (showHelp) {
            viewHolder.getFloatingButtonText().setVisibility(View.VISIBLE);
            fade_out.setDuration(2000);
            fade_out.setStartOffset(5000);
            fade_out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    showHelp = false;
                    viewHolder.getFloatingButtonText().setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            viewHolder.getFloatingButtonText().startAnimation(fade_out);
        }
    }


    @Override
    public void onTrimMemory(int level) {
        if (level == TRIM_MEMORY_BACKGROUND || level == TRIM_MEMORY_RUNNING_LOW) {
            imageCacheHandler.getCache().clearMemory();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override
    public void onLowMemory() {
    }
}
