package com.github.st1hy.sabre;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.st1hy.imagecache.CacheProvider;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.core.utils.Utils;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.TextView;

public class SettingsFragment extends Fragment {
    public static final String PREF_ENABLE_OPEN_GL = "com.github.st1hy.preferences.enable_open_gl";
    public static final boolean PREF_ENABLE_OPEN_GL_DEFAULT = true;

    private SharedPreferences preferences;
    private int rippleColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getPreferences(getActivity());
        rippleColor = Utils.getColor(getActivity(), R.color.settings_ripple);
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
        setupEnableOpenGL(root);
        setupClearCache(root);
        return root;
    }

    private void setupEnableOpenGL(View root) {
        View preference = root.findViewById(R.id.settings_use_open_gl);
        TextView title = (TextView) preference.findViewById(R.id.text_setting_title);
        title.setText(R.string.pref_use_open_gl_title);
        TextView subtitle = (TextView) preference.findViewById(R.id.text_setting_subtitle);
        subtitle.setText(R.string.pref_use_open_gl_desc);
        final CheckBox checkBox = (CheckBox) root.findViewById(R.id.setting_checkbox);
        checkBox.setCheckedImmediately(isOpenGLEnabled(preferences));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean(PREF_ENABLE_OPEN_GL, isChecked).apply();
            }
        });
        preference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = checkBox.isChecked();
                checkBox.setChecked(!checked);
            }
        });
        MaterialRippleLayout.on(preference).rippleColor(rippleColor).rippleAlpha(1f).create();
    }

    private void setupClearCache(View root) {
        View preference = root.findViewById(R.id.setting_clear_cache);
        TextView title = (TextView) preference.findViewById(R.id.text_setting_title);
        title.setText(R.string.pref_clear_cache_title);
        TextView subtitle = (TextView) preference.findViewById(R.id.text_setting_subtitle);
        subtitle.setText(R.string.pref_clear_cache_desc);
        preference.findViewById(R.id.setting_checkbox).setVisibility(View.GONE);
        preference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ImageCache cache = ((CacheProvider) getActivity()).getCacheHandler().getCache();
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        cache.clearCache();
                    }
                });
            }
        });
        MaterialRippleLayout.on(preference).rippleColor(rippleColor).rippleAlpha(1f).create();
    }

    public static void loadDefaultSettings(Context context, boolean overrideCurrentValues) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if (overrideCurrentValues || !preferences.contains(PREF_ENABLE_OPEN_GL)) {
            editor.putBoolean(PREF_ENABLE_OPEN_GL, PREF_ENABLE_OPEN_GL_DEFAULT);
        }
        editor.apply();
    }

    public static boolean isOpenGLEnabled(Context context) {
        return isOpenGLEnabled(getPreferences(context));
    }

    private static boolean isOpenGLEnabled(SharedPreferences preferences) {
        return preferences.getBoolean(PREF_ENABLE_OPEN_GL, PREF_ENABLE_OPEN_GL_DEFAULT);
    }

    public static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
