package com.github.st1hy.sabre.core.injector.component;

import android.app.Activity;

import com.github.st1hy.sabre.core.injector.PerActivity;
import com.github.st1hy.sabre.core.injector.module.ActivityModule;
import com.github.st1hy.sabre.dao.DaoSession;
import com.github.st1hy.sabre.history.HistoryActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {
    DaoSession daoSession();
    Activity activity();

    void inject(HistoryActivity activity);
}
