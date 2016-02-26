package com.github.st1hy.sabre.history;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import com.github.st1hy.dao.OpenedImageContentProvider;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.imagecache.ImageCacheProvider;
import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.image.ImageActivity;
import com.google.common.base.Preconditions;

public class HistoryFragment extends Fragment implements HistoryRecyclerAdapter.OnImageClicked {
    private static final int REQUEST_IMAGE = 0x16ed;
    private final HistoryViewHolder viewHolder = new HistoryViewHolder();
    private static final String SAVE_ANIMATION_SHOW_FLAG = "animation shown";
    private boolean showHelp = true;
    private HistoryRecyclerAdapter historyAdapter;
    private Animation fade_out;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sanityCheck();
        setHasOptionsMenu(true);
        fade_out = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        showHelp = needShowHelp(savedInstanceState);
        ImageCache imageCache = ((ImageCacheProvider) getActivity()).getImageCacheHandler().getCache();
        historyAdapter = new HistoryRecyclerAdapter(getActivity(), imageCache, this);

        Application app = (Application) getActivity().getApplication();
        DaoMaster daoMaster = app.getCache().getInstance(DaoMaster.class);
        Preconditions.checkNotNull(daoMaster);
        OpenedImageContentProvider.daoSession = daoMaster.newSession();
    }

    private void sanityCheck() {
        MissingInterfaceException.parentSanityCheck(this, ImageCacheProvider.class);
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
        viewHolder.bind(root);
        viewHolder.getRecyclerView().setAdapter(historyAdapter);
        viewHolder.getRecyclerView().setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        viewHolder.getFloatingButtonText().setVisibility(View.GONE);
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
        viewHolder.getFloatingButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionOpen();
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder.unbind();
    }

    private void onActionOpen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE);
    }


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
    public void openImage(@NonNull Uri uri) {
        Intent intent = new Intent();
        intent.setDataAndTypeAndNormalize(uri, "image/*");
        intent.setAction(Intent.ACTION_VIEW);
        intent.setClass(getActivity(), ImageActivity.class);
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            startActivityWithExitTransition(intent);
//        } else {
//            startActivity(intent);
//        }
        startActivity(intent);
    }

    @TargetApi(21)
    private void startActivityWithExitTransition(Intent intent) {
        Bundle bundle = ActivityOptions
                .makeSceneTransitionAnimation(getActivity())
                .toBundle();
        startActivity(intent, bundle);
    }
}
