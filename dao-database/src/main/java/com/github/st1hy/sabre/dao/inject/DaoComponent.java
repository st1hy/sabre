package com.github.st1hy.sabre.dao.inject;

import com.github.st1hy.sabre.dao.DaggerOpenedImageContentProvider;
import com.github.st1hy.sabre.dao.DaoSession;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = DaoMasterModule.class)
@Singleton
public interface DaoComponent {

    DaoSession session();

    void inject(DaggerOpenedImageContentProvider contentProvider);

}
