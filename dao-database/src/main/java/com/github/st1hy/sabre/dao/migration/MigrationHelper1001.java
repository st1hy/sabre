package com.github.st1hy.sabre.dao.migration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.github.st1hy.core.utils.Utils;

import java.io.File;

import timber.log.Timber;

/**
 * Performs database update from scheme version 1000 to scheme 1001.
 */
public class MigrationHelper1001 implements MigrationHelper {
    public static final int FROM = 1000, TO = 1001;

    private static final String TABLE = "OPENED_IMAGE";
    private static final String ID = "_id";
    private static final String URI = "URI";
    private static final String DATE = "DATE";

    private static final String FILENAME = "FILENAME";

    private final Context context;

    public MigrationHelper1001(@NonNull Context context) {
        this.context = context;
    }

    @Override
    public boolean onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.i("Upgrading images database from 1000 to 1001");
        db.beginTransaction();
        try {
            final String TABLE_OLD = TABLE + "_OLD";
            db.execSQL("alter table " + TABLE + " rename to " + TABLE_OLD);
            createTable1001(db);

            String[] columns = new String[]{ID, URI, DATE};
            Cursor cursor = db.query(TABLE_OLD, columns, null, null, null, null, null);
            while (cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
                String uri = cursor.getString(cursor.getColumnIndexOrThrow(URI));
                int date = cursor.getInt(cursor.getColumnIndexOrThrow(DATE));
                values.put(ID, id);
                values.put(URI, uri);
                values.put(DATE, date);
                File file = Utils.getRealPathFromURI(context, Uri.parse(uri));
                if (file != null) {
                    values.put(FILENAME, file.getName());
                }
                db.insert(TABLE, null, values);
            }
            cursor.close();
            db.execSQL("drop table " + TABLE_OLD);
            db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            Timber.e(e, "Upgrading images database failed!");
        } finally {
            db.endTransaction();
            Timber.i("Upgrading images database from 1000 to 1001 successful!");
        }
        return false;
    }

    private void createTable1001(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + "\"OPENED_IMAGE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE ," + // 0: id
                "\"URI\" TEXT NOT NULL UNIQUE ," + // 1: uri
                "\"FILENAME\" TEXT," + // 2: filename
                "\"DATE\" INTEGER NOT NULL );"); // 3: date
        // Add Indexes
        db.execSQL("CREATE INDEX " + "IDX_OPENED_IMAGE__id ON OPENED_IMAGE" +
                " (\"_id\");");
    }
}
