package com.github.st1hy.sabre.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.ui.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity {
//    public static final ButterKnife.Setter<View, Integer> RIPPLE = new ButterKnife.Setter<View, Integer>() {
//        @Override
//        public void set(@NonNull View view, @ColorInt Integer value, int index) {
//            MaterialRippleLayout.on(view).rippleColor(value).rippleAlpha(1f).create();
//        }
//    };

//    @BindColor(R.color.settings_ripple)
//    int rippleColor;
//    @Bind({})
//    List<View> settingViews;
    @Bind(R.id.settings_toolbar)
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

//    private void bind() {
//        ButterKnife.apply(settingViews, RIPPLE, rippleColor);
//    }

    public static void loadDefaultSettings(Context context, boolean overrideCurrentValues) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.apply();
    }
}
