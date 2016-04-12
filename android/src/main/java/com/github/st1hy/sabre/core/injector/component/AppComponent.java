package com.github.st1hy.sabre.core.injector.component;

import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.dao.DaoSession;
import com.github.st1hy.sabre.core.injector.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = ApplicationModule.class)
@Singleton
public interface AppComponent {
    DaoSession session();

    void inject(Application app);
}
