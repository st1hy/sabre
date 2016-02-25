package com.github.st1hy.dao.mitigation;

import android.database.sqlite.SQLiteDatabase;

public interface MitigationHelper {

    /**
     *
     * @param db opened old version of database
     * @param oldVersion old db version number
     * @param newVersion new db version number
     * @return true if upgrade had not encountered any problems
     */
    boolean onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
