package com.github.st1hy.sabre.history;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.Space;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.cache.ImageCache;
import com.github.st1hy.sabre.core.cache.worker.CacheEntryNameFactory;
import com.github.st1hy.sabre.core.cache.worker.DrawableImageWorker;
import com.github.st1hy.sabre.core.cache.worker.ImageWorker;
import com.github.st1hy.sabre.core.cache.worker.SimpleLoaderFactory;
import com.github.st1hy.sabre.history.content.HistoryContentProvider;
import com.github.st1hy.sabre.history.content.HistoryTable;

import java.text.DateFormat;
import java.util.Date;

public class HistoryAdapter extends SimpleCursorAdapter implements LoaderManager.LoaderCallbacks<Cursor> {
    private final Context context;
    private static final String[] FROM = new String[] {HistoryTable.COLUMN_URI, HistoryTable.COLUMN_DATE_TIMESTAMP};
    private static final int[] TO = new int[] {R.id.entry_image, R.id.entry_date};
    private final int firstElementHeight;
    private int numColumns = 1;
    private final ImageWorker<Drawable> imageWorker;

    public HistoryAdapter(Context context, ImageCache imageCache) {
        super(context, R.layout.history_entry_layout, null, FROM, TO, 0);
        this.context = context;
        this.firstElementHeight = (int) context.getResources().getDimension(R.dimen.actionBarHeight);
        this.imageWorker = new DrawableImageWorker(context, imageCache);
        imageWorker.setCacheEntryNameFactory(new CacheEntryNameFactory() {
            @Override
            public String getCacheIndex(Uri uri) {
                return ".".concat(uri.getPath()).concat(".thumb");
            }
        });
        imageWorker.setLoaderFactory(SimpleLoaderFactory.RESULT_ON_MAIN_THREAD);
        final int thumbSize = (int) context.getResources().getDimension(R.dimen.history_thumb_size);
        imageWorker.setRequestedSize(thumbSize, thumbSize);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {HistoryTable.COLUMN_ID, HistoryTable.COLUMN_DATE_TIMESTAMP, HistoryTable.COLUMN_URI};
        String sortingOrder = HistoryTable.COLUMN_DATE_TIMESTAMP + " DESC";
        return new CursorLoader(context, HistoryContentProvider.CONTENT_URI, projection, null, null, sortingOrder);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return position < numColumns ?
                null : super.getItem(position - numColumns);
    }

    @Override
    public int getCount() {
        return super.getCount() + numColumns;
    }

    @Override
    public long getItemId(int position) {
        return position < numColumns ? 0 : position - numColumns;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < numColumns) ? 1 : 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position < numColumns) {
            if (convertView == null) {
                convertView = supplySpaceView(parent);
            }
            return convertView;
        } else {
            return super.getView(position - numColumns, convertView, parent);
        }
    }

    @Override
    public void setViewImage(final ImageView v, String value) {
        Uri uri = Uri.parse(value);
        imageWorker.loadImage(uri, (ImageReceiverView) v);
    }

    private View supplySpaceView(ViewGroup parent) {
        Space space = new Space(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(0, firstElementHeight);
        space.setLayoutParams(layoutParams);
        return space;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
    }

    @Override
    public void setViewText(TextView v, String text) {
        if (text.startsWith("content")) {
            super.setViewText(v, text);
        } else {
            v.setText(DateFormat.getDateTimeInstance().format(new Date(Long.valueOf(text))));
        }
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swapCursor(null);
    }

    public void onDestroy() {
        imageWorker.setExitTasksEarly(true);
    }
}
