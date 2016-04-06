package com.github.st1hy.sabre.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.st1hy.sabre.dao.migration.MigrationHelper1001;

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
            Timber.w("Upgrading schema from version %d to %d failed. Upgrading by dropping all tables!", oldVersion, newVersion);
            DaoMaster.dropAllTables(db, true);
            onCreate(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.w("Downgrading database from %d to %d unsupported! Dropping all tables", oldVersion, newVersion);
        DaoMaster.dropAllTables(db, true);
        onCreate(db);
    }

    private boolean upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean success = false;
        if (oldVersion == MigrationHelper1001.FROM && newVersion == MigrationHelper1001.TO) {
            success = new MigrationHelper1001(context).onUpgrade(db, oldVersion, newVersion);
        }
        return success;
    }


}
