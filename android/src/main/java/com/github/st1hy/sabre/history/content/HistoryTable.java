package com.github.st1hy.sabre.history.content;

import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HistoryTable {
    // Database table
    public static final String TABLE_LAST_VIEWED = "todo";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URI = "uri";
    public static final String COLUMN_DATE_TIMESTAMP = "date";
    public static final List<String> AVAILABLE_COLUMNS = Collections.unmodifiableList(Arrays.asList(COLUMN_ID, COLUMN_URI, COLUMN_DATE_TIMESTAMP)) ;

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_LAST_VIEWED
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DATE_TIMESTAMP + " integer not null, "
            + COLUMN_URI + " text not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_LAST_VIEWED);
        onCreate(database);
    }
}
