package com.github.st1hy.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.st1hy.dao.mitigation.MitigationHelper1001;

import timber.log.Timber;

/**
 * Production open helper with support for upgrading schemes.
 */
public class DaoOpenHelper extends DaoMaster.OpenHelper {
    private final Context context;

    public DaoOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
        this.context = context;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean updateSuccessful = upgrade(db, oldVersion, newVersion);
        if (!updateSuccessful) {
            Timber.w("Upgrading schema from version " + oldVersion + " to " + newVersion + " failed. Upgrading by dropping all tables!");
            DaoMaster.dropAllTables(db, true);
            onCreate(db);
        }
    }

    private boolean upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean success = false;
        if (oldVersion == MitigationHelper1001.FROM && newVersion == MitigationHelper1001.TO) {
            success = new MitigationHelper1001(context).onUpgrade(db, oldVersion, newVersion);
        }
        return success;
    }


}
