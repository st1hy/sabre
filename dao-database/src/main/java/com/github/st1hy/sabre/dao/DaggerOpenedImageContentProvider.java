package com.github.st1hy.sabre.dao;

import android.database.sqlite.SQLiteDatabase;

import com.github.st1hy.sabre.dao.inject.DaggerDaoComponent;
import com.github.st1hy.sabre.dao.inject.DaoComponent;
import com.github.st1hy.sabre.dao.inject.DaoMasterModule;

import javax.inject.Inject;

import dagger.Lazy;

public class DaggerOpenedImageContentProvider extends OpenedImageContentProvider {

    @Inject
    Lazy<DaoSession> session;

    @Override
    public boolean onCreate() {
        DaoComponent component = DaggerDaoComponent.builder()
                .daoMasterModule(new DaoMasterModule(getContext()))
                .build();
        component.inject(this);
        return super.onCreate();
    }

    @Override
    protected SQLiteDatabase getDatabase() {
        return getSession().getDatabase();
    }

    public DaoSession getSession() {
        return session.get();
    }
}
