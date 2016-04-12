package com.github.st1hy.sabre.core.ui;

import android.support.v7.app.AppCompatActivity;

import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.core.injector.component.ActivityComponent;
import com.github.st1hy.sabre.core.injector.component.AppComponent;
import com.github.st1hy.sabre.core.injector.component.DaggerActivityComponent;
import com.github.st1hy.sabre.core.injector.module.ActivityModule;

public class BaseActivity extends AppCompatActivity {
    private ActivityComponent component;

    protected ActivityComponent getComponent() {
        if (component == null) {
            component = DaggerActivityComponent.builder()
                    .activityModule(new ActivityModule(this))
                    .appComponent(appComponent())
                    .build();
        }
        return component;
    }

    private AppComponent appComponent() {
        return ((Application) getApplication()).getComponent();
    }

}
