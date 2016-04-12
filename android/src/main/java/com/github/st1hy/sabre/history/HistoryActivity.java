package com.github.st1hy.sabre.history;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.ui.BaseActivity;
import com.github.st1hy.sabre.history.utils.ForwardingLoaderCallback;
import com.github.st1hy.sabre.history.utils.SimpleAnimationListener;
import com.github.st1hy.sabre.settings.SettingsActivity;

import javax.inject.Inject;

public class HistoryActivity extends BaseActivity {
    private static final int REQUEST_IMAGE = 0x16ed;
    private static final String SAVE_ANIMATION_SHOW_FLAG = "animation shown";

    private boolean showHelp = true;
    private Animation fade_out;

    @Inject
    HistoryRecyclerAdapter historyAdapter;
    @Inject
    HistoryViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getComponent().inject(this);

        fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        showHelp = needShowHelp(savedInstanceState);
        SettingsActivity.loadDefaultSettings(this, false);
        setSupportActionBar(viewHolder.getToolbar());
        onViewCreated();
    }

    private boolean needShowHelp(Bundle savedInstanceState) {
        return savedInstanceState == null || savedInstanceState.getBoolean(SAVE_ANIMATION_SHOW_FLAG, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        historyAdapter.onStop();
    }

    private void onViewCreated() {
        viewHolder.getRecyclerView().setAdapter(historyAdapter);
        viewHolder.getRecyclerView().setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        viewHolder.getFloatingButtonText().setVisibility(View.GONE);
        viewHolder.getFloatingButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionOpen();
            }
        });
        getSupportLoaderManager().initLoader(0, null, new ForwardingLoaderCallback(historyAdapter) {
            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                super.onLoadFinished(loader, data);
                onDataLoaded(data);
            }
        });
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

    //Called using menu_history.xml
    public void onActionOpen(final MenuItem item) {
        onActionOpen();
    }

    private void onActionOpen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE);
    }


    //Called using menu_history.xml
//    public void onActionSettings(final MenuItem item) {
//        Intent intent = new Intent(this, SettingsActivity.class);
//        startActivity(intent);
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE:
                if (resultCode == Activity.RESULT_OK && null != data) {
                    Uri uri = data.getData();
                    historyAdapter.openImage(uri);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onDataLoaded(Cursor data) {
        if (data.getCount() > 0) {
            showHelp = false;
            viewHolder.getEmptyView().setVisibility(View.GONE);
        } else if (showHelp) {
            viewHolder.getFloatingButtonText().setVisibility(View.VISIBLE);
            fade_out.setDuration(2000);
            fade_out.setStartOffset(5000);
            fade_out.setAnimationListener(new SimpleAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    showHelp = false;
                    viewHolder.getFloatingButtonText().setVisibility(View.GONE);
                }
            });
            viewHolder.getFloatingButtonText().startAnimation(fade_out);
        }
    }
}
