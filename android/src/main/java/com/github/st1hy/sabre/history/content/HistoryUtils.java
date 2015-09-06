package com.github.st1hy.sabre.history.content;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.github.st1hy.gesturedetector.Config;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public enum HistoryUtils {
    ;

    public static void updateDatabaseWithImage(Context context, Uri uri, Date date, boolean deleteDuplicates) {
        String[] projection =  new String[] {HistoryTable.COLUMN_ID};
        String[] selectionArgs = new String[] { uri.toString() };
        String selection = HistoryTable.COLUMN_URI +  " = ?";
        String idSelection = HistoryTable.COLUMN_ID +  " = ?";

        ContentValues values = new ContentValues();
        values.put(HistoryTable.COLUMN_URI, uri.toString());
        values.put(HistoryTable.COLUMN_DATE_TIMESTAMP, String.valueOf(date.getTime()));

        ContentResolver resolver = context.getContentResolver();
        ContentProviderClient contentProviderClient = resolver.acquireContentProviderClient(HistoryContentProvider.CONTENT_URI);
        HistoryContentProvider historyContentProvider = (HistoryContentProvider) contentProviderClient.getLocalContentProvider();
        if (historyContentProvider != null) {
            ReentrantLock lock = historyContentProvider.getLock();
            lock.lock();

            Cursor query = historyContentProvider.query(HistoryContentProvider.CONTENT_URI, projection, selection, selectionArgs, null);
            int lastEntryId = -1;
            if (query.moveToNext()) {
                lastEntryId = query.getInt(query.getColumnIndexOrThrow(HistoryTable.COLUMN_ID));
            }
            if (deleteDuplicates) {
                List<Integer> duplicateEntries = new LinkedList<>();
                while (query.moveToNext()) {
                    int duplicate = query.getInt(query.getColumnIndexOrThrow(HistoryTable.COLUMN_ID));
                    duplicateEntries.add(duplicate);
                }
                for (int id: duplicateEntries) {
                    historyContentProvider.delete(HistoryContentProvider.CONTENT_URI, idSelection, new String[] {String.valueOf(id)});
                }
            }
            query.close();
            if (lastEntryId == -1) {
                historyContentProvider.insert(HistoryContentProvider.CONTENT_URI, values);
            } else {
                String[] updateArgs = new String[] {String.valueOf(lastEntryId)};
                historyContentProvider.update(HistoryContentProvider.CONTENT_URI, values, idSelection, updateArgs);
            }

            lock.unlock();
        } else if (Config.DEBUG) {
            Log.e("DATABASE", "Could not find history content provider on the same process! Data has not been saved or modified.");
        }
        contentProviderClient.release();
    }
}
