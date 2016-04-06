package com.github.st1hy.sabre.dao.migration;

import android.database.sqlite.SQLiteDatabase;

public interface MigrationHelper {

    /**
     * @param db         opened old version of database
     * @param oldVersion old db version number
     * @param newVersion new db version number
     * @return true if upgrade had not encountered any problems
     */
    boolean onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
