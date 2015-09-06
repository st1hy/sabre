package com.github.st1hy.sabre.history;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.util.SystemUIMode;

public class HistoryFragment extends Fragment {
    private HistoryViewDelegate viewDelegate;
    private static final String SAVE_ANIMATION_SHOW_FLAG = "animation shown";
    private boolean showHelp = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        showHelp = needShowHelp(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.app_name);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        viewDelegate = new HistoryViewDelegate(root);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (showHelp) {
            Animation fade_out = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
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
        } else {
            viewDelegate.getFloatingButtonText().setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        SystemUIMode.DEFAULT.apply(getActivity().getWindow());
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
