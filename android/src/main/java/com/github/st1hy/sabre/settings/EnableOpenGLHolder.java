package com.github.st1hy.sabre.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.injector.ViewBinder;
import com.rey.material.widget.CheckBox;

import butterknife.OnCheckedChanged;

public class EnableOpenGLHolder extends SettingOnOffHolder implements View.OnClickListener {
    public static final String PREF_ENABLE_OPEN_GL = "com.github.st1hy.preferences.enable_open_gl";
    public static final boolean PREF_ENABLE_OPEN_GL_DEFAULT = true;

    private final SharedPreferences preferences;

    EnableOpenGLHolder(@NonNull SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public ViewBinder bind(@NonNull View view) {
        super.bind(view);
        setTitle(R.string.pref_use_open_gl_title);
        setSubTitle(R.string.pref_use_open_gl_desc);
        setCheckedImmediately(isOpenGLEnabled(preferences));
        view.setOnClickListener(this);
        return this;
    }

    @OnCheckedChanged(R.id.setting_checkbox)
    void onCheckedChanged(boolean isChecked) {
        preferences.edit().putBoolean(PREF_ENABLE_OPEN_GL, isChecked).apply();
    }

    @Override
    public void onClick(View v) {
        CheckBox checkBox = getCheckBox();
        boolean checked = checkBox.isChecked();
        checkBox.setChecked(!checked);
    }

    public static boolean isOpenGLEnabled(Context context) {
        return isOpenGLEnabled(PreferenceManager.getDefaultSharedPreferences(context));
    }

    private static boolean isOpenGLEnabled(SharedPreferences preferences) {
        return preferences.getBoolean(PREF_ENABLE_OPEN_GL, PREF_ENABLE_OPEN_GL_DEFAULT);
    }
}
