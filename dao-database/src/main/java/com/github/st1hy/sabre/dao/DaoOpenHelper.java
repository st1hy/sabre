package com.github.st1hy.sabre.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.st1hy.sabre.dao.migration.MigrationHelper;
import com.github.st1hy.sabre.dao.migration.MigrationHelper1001;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

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

    /**
     * @return true if database was successfully upgraded.
     */
    private boolean upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MigrationHelper migrationHelper = getMigrationPolicy(context).get(oldVersion, newVersion);
        if (migrationHelper != null) {
            return migrationHelper.onUpgrade(db, oldVersion, newVersion);
        } else {
            Timber.w("No database migration from %d to %d available!", oldVersion, newVersion);
            return false;
        }
    }

    private static Table<Integer, Integer, MigrationHelper> getMigrationPolicy(Context context) {
        Table<Integer, Integer, MigrationHelper> migrationHelperTable = HashBasedTable.create();
        migrationHelperTable.put(MigrationHelper1001.FROM, MigrationHelper1001.TO, new MigrationHelper1001(context));

        return migrationHelperTable;
    }

}
