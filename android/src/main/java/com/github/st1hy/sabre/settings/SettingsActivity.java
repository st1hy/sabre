package com.github.st1hy.sabre.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.st1hy.imagecache.ImageCacheHandler;
import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.CacheUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {
    public static final ButterKnife.Setter<View, Integer> RIPPLE = new ButterKnife.Setter<View, Integer>() {
        @Override
        public void set(@NonNull View view, @ColorInt Integer value, int index) {
            MaterialRippleLayout.on(view).rippleColor(value).rippleAlpha(1f).create();
        }
    };

    @BindColor(R.color.settings_ripple)
    int rippleColor;
    @Bind({R.id.settings_use_open_gl, R.id.setting_clear_cache})
    List<View> settingViews;
    @Bind(R.id.settings_toolbar)
    Toolbar toolbar;

    private ImageCacheHandler imageCacheHandler;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        imageCacheHandler = CacheUtils.newImageCacheHandler((Application) getApplication());
        setContentView(R.layout.activity_settings);
        bind();
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void bind() {
        ButterKnife.bind(this);
        new EnableOpenGLHolder(preferences).bind(settingViews.get(0));
        new ClearCacheHolder(imageCacheHandler).bind(settingViews.get(1));
        ButterKnife.apply(settingViews, RIPPLE, rippleColor);
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
