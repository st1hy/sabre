package com.github.st1hy.sabre.history.content;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Modified version content provider from http://www.vogella.com/tutorials/AndroidSQLite/article.html
 *
 */
public class HistoryContentProvider extends ContentProvider {
    private static final String AUTHORITY = "com.github.st1hy.sabre.history.content.provider";
    private static final String BASE_PATH = "history";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE+ "/" + BASE_PATH;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BASE_PATH;

    // used for the UriMacher
    private static final int ENTRY = 10;
    private static final int ENTRY_ID = 20;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, ENTRY);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ENTRY_ID);
    }

    private HistoryDatabaseHelper database;
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public boolean onCreate() {
        database = new HistoryDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(HistoryTable.TABLE_LAST_VIEWED);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case ENTRY:
                break;
            case ENTRY_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(HistoryTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        lock.lock();
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        lock.unlock();
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        lock.lock();
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;
        switch (uriType) {
            case ENTRY:
                id = sqlDB.insert(HistoryTable.TABLE_LAST_VIEWED, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        lock.unlock();
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        lock.lock();
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case ENTRY:
                rowsDeleted = sqlDB.delete(HistoryTable.TABLE_LAST_VIEWED, selection,
                        selectionArgs);
                break;
            case ENTRY_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(HistoryTable.TABLE_LAST_VIEWED,
                            HistoryTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(HistoryTable.TABLE_LAST_VIEWED,
                            HistoryTable.COLUMN_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        lock.unlock();
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        lock.lock();
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
            case ENTRY:
                rowsUpdated = sqlDB.update(HistoryTable.TABLE_LAST_VIEWED,
                        values,
                        selection,
                        selectionArgs);
                break;
            case ENTRY_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(HistoryTable.TABLE_LAST_VIEWED,
                            values,
                            HistoryTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(HistoryTable.TABLE_LAST_VIEWED,
                            values,
                            HistoryTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        lock.unlock();
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    private void checkColumns(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<>(HistoryTable.AVAILABLE_COLUMNS);
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
