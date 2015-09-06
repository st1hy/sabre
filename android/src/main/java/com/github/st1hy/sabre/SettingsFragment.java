package com.github.st1hy.sabre;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;

import com.github.st1hy.sabre.core.util.SystemUIMode;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.TextView;

public class SettingsFragment extends Fragment {
    public static final String PREF_ENABLE_OPEN_GL = "com.github.st1hy.preferences.enable_open_gl";
    public static final boolean PREF_ENABLE_OPEN_GL_DEFAULT = true;

    private SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getPreferences(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        SystemUIMode.DEFAULT.apply(getActivity().getWindow());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        setupEnableOpenGL(root);
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
