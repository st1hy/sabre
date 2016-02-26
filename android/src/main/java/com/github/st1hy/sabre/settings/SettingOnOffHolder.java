package com.github.st1hy.sabre.settings;

import android.support.annotation.StringRes;
import android.widget.TextView;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.injector.ViewBinder;
import com.rey.material.widget.CheckBox;

import butterknife.Bind;

class SettingOnOffHolder extends ViewBinder {

    @Bind(R.id.text_setting_title)
    TextView title;
    @Bind(R.id.text_setting_subtitle)
    TextView subTitle;
    @Bind(R.id.setting_checkbox)
    CheckBox checkBox;

    public void setTitle(@StringRes int stringResId) {
        title.setText(stringResId);
    }

    public void setSubTitle(@StringRes int stringResId) {
        subTitle.setText(stringResId);
    }

    public void setCheckedImmediately(boolean checked) {
        checkBox.setCheckedImmediately(checked);
    }

    public TextView getTitle() {
        return title;
    }

    public TextView getSubTitle() {
        return subTitle;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }
}
