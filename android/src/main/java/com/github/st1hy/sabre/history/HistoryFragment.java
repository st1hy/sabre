package com.github.st1hy.sabre.history;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.st1hy.core.utils.MissingInterfaceException;
import com.github.st1hy.dao.DaoMaster;
import com.github.st1hy.dao.DaoSession;
import com.github.st1hy.dao.OpenImageUtils;
import com.github.st1hy.dao.OpenedImageContentProvider;
import com.github.st1hy.imagecache.CacheProvider;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.NavState;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.image.ImageActivity;

import java.util.Date;

public class HistoryFragment extends Fragment implements HistoryRecyclerAdapter.OnImageClicked {
    private static final int REQUEST_IMAGE = 0x16ed;
    private HistoryViewDelegate viewDelegate;
    private static final String SAVE_ANIMATION_SHOW_FLAG = "animation shown";
    private boolean showHelp = true;
    private HistoryRecyclerAdapter historyAdapter;
    private Animation fade_out;

    private DaoSession daoSession;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sanityCheck();
        setHasOptionsMenu(true);
        fade_out = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        showHelp = needShowHelp(savedInstanceState);
        ImageCache imageCache = ((CacheProvider) getActivity()).getCacheHandler().getCache();
        historyAdapter = new HistoryRecyclerAdapter(getActivity(), imageCache, this);

        DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(getActivity(), "images.db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        OpenedImageContentProvider.daoSession = daoSession;
    }

    private void sanityCheck() {
        MissingInterfaceException.parentSanityCheck(this, CacheProvider.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        historyAdapter.onDestroy();
        OpenedImageContentProvider.daoSession = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.app_name);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        viewDelegate = new HistoryViewDelegate(root);
        viewDelegate.getRecyclerView().setAdapter(historyAdapter);
        viewDelegate.getRecyclerView().setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        viewDelegate.getFloatingButtonText().setVisibility(View.GONE);
        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
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
        });
        viewDelegate.getFloatingButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionOpen();
            }
        });
        return root;
    }

    private void onActionOpen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE);
    }


    private void onDataLoaded(Cursor data) {
        if (data.getCount() > 0) {
            showHelp = false;
            viewDelegate.getEmptyView().setVisibility(View.GONE);
        } else if (showHelp) {
            viewDelegate.getFloatingButtonText().setVisibility(View.VISIBLE);
            fade_out.setDuration(2000);
            fade_out.setStartOffset(5000);
            fade_out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    showHelp = false;
                    viewDelegate.getFloatingButtonText().setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            viewDelegate.getFloatingButtonText().startAnimation(fade_out);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_history, menu);
        menu.findItem(R.id.action_open).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onActionOpen();
                return true;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_ANIMATION_SHOW_FLAG, showHelp);
    }

    private boolean needShowHelp(Bundle savedInstanceState) {
        return savedInstanceState == null || savedInstanceState.getBoolean(SAVE_ANIMATION_SHOW_FLAG, true);
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
    public void openImage(final Uri uri) {
        final Date date = new Date();
        Application.CACHED_EXECUTOR_POOL.execute(new Runnable() {
            @Override
            public void run() {
                OpenImageUtils.updateOpenedImage(getActivity(), daoSession, uri, date);
            }
        });
        final Bundle arguments = new Bundle();
        arguments.putParcelable(NavState.ARG_IMAGE_URI, uri);
        Intent intent = new Intent(getActivity(), ImageActivity.class);
        intent.putExtras(arguments);
        startActivity(intent);
    }
}
