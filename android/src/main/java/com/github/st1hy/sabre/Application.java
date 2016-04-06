package com.github.st1hy.sabre;

import android.database.sqlite.SQLiteDatabase;

import com.github.st1hy.collect.MutableRestrictiveClassToInstanceMap;
import com.github.st1hy.collect.RestrictiveClassToInstanceMap;
import com.github.st1hy.sabre.dao.DaoMaster;
import com.github.st1hy.sabre.dao.DaoOpenHelper;
import com.github.st1hy.gesturedetector.Config;

import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public class Application extends android.app.Application {
    private final RestrictiveClassToInstanceMap map = MutableRestrictiveClassToInstanceMap.create(new ConcurrentHashMap<Class<?>, Object>());

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Config.DEBUG = BuildConfig.DEBUG;
        configureDao();
    }

    private void configureDao() {
        DaoOpenHelper helper = new DaoOpenHelper(this, "images.db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        map.putInstance(DaoMaster.class, new DaoMaster(db));
    }

    public RestrictiveClassToInstanceMap getCache() {
        return map;
    }
}
