package com.github.st1hy.sabre.dao.inject;

import android.content.Context;

import com.github.st1hy.sabre.dao.DaoMaster;
import com.github.st1hy.sabre.dao.DaoOpenHelper;
import com.github.st1hy.sabre.dao.DaoSession;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DaoMasterModule {
    protected final Context context;

    public DaoMasterModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    public DaoOpenHelper provideDaoOpenHelper() {
        return new DaoOpenHelper(context, "images.db", null);
    }

    @Provides
    @Singleton
    public DaoMaster provideDaoMaster(DaoOpenHelper db) {
        return new DaoMaster(db.getWritableDatabase());
    }

    @Provides
    @Singleton
    public DaoSession provideDaoSession(DaoMaster daoMaster) {
        return daoMaster.newSession();
    }
}
