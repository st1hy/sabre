package com.github.st1hy.sabre.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.st1hy.imagecache.CacheProvider;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.injector.ViewBinder;

import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

public class SettingsFragment extends Fragment {

    public static final ButterKnife.Setter<View, Integer> RIPPLE = new ButterKnife.Setter<View, Integer>() {
        @Override
        public void set(@NonNull View view, @ColorInt Integer value, int index) {
            MaterialRippleLayout.on(view).rippleColor(value).rippleAlpha(1f).create();
        }
    };
    private SharedPreferences preferences;

    @BindColor(R.color.settings_ripple)
    int rippleColor;


    @Bind({R.id.settings_use_open_gl, R.id.setting_clear_cache})
    List<View> settingViews;
    private final List<ViewBinder> viewBinders = new LinkedList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.action_settings);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, root);
        viewBinders.add(new EnableOpenGLHolder(preferences).bind(settingViews.get(0)));
        viewBinders.add(new ClearCacheHolder((CacheProvider) getActivity()).bind(settingViews.get(1)));
        ButterKnife.apply(settingViews, RIPPLE, rippleColor);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        for (ViewBinder binder : viewBinders) binder.unbind();
    }

    public static void loadDefaultSettings(Context context, boolean overrideCurrentValues) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if (overrideCurrentValues || !preferences.contains(EnableOpenGLHolder.PREF_ENABLE_OPEN_GL)) {
            editor.putBoolean(EnableOpenGLHolder.PREF_ENABLE_OPEN_GL, EnableOpenGLHolder.PREF_ENABLE_OPEN_GL_DEFAULT);
        }
        editor.apply();
    }
}
