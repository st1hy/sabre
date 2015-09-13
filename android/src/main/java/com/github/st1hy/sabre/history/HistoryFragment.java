package com.github.st1hy.sabre.history;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.cache.CacheProvider;
import com.github.st1hy.sabre.core.cache.ImageCache;
import com.github.st1hy.sabre.core.util.MissingInterfaceException;

public class HistoryFragment extends Fragment {
    private HistoryViewDelegate viewDelegate;
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
        ImageCache imageCache = ((CacheProvider) getActivity()).getCacheHandler().getCache();
        historyAdapter = new HistoryRecyclerAdapter(getActivity(), imageCache);
    }

    private void sanityCheck() {
        MissingInterfaceException.parentSanityCheck(this, CacheProvider.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        historyAdapter.onDestroy();
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
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_ANIMATION_SHOW_FLAG, showHelp);
    }

    private boolean needShowHelp(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getBoolean(SAVE_ANIMATION_SHOW_FLAG, true);
        }
        return true;
    }
}
