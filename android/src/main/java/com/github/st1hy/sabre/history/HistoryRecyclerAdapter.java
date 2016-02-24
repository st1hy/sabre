package com.github.st1hy.sabre.history;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.st1hy.core.utils.Utils;
import com.github.st1hy.dao.OpenedImageContentProvider;
import com.github.st1hy.dao.OpenedImageDao;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.imagecache.worker.CacheEntryNameFactory;
import com.github.st1hy.imagecache.worker.DrawableImageWorker;
import com.github.st1hy.imagecache.worker.ImageWorker;
import com.github.st1hy.imagecache.worker.SimpleLoaderFactory;
import com.github.st1hy.sabre.R;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryEntryHolder> implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int HISTORY_ENTRY = R.layout.history_entry_layout;
    private static final int SEPARATOR_ENTRY = R.layout.history_entry_top_separator;

    private final Context context;
    private final ImageWorker<Drawable> imageWorker;
    private final OnImageClicked onImageClicked;
    private int numColumns = 1;
    private Cursor cursor;

    public HistoryRecyclerAdapter(Context context, ImageCache imageCache, OnImageClicked onImageClicked) {
        this.context = context;
        this.onImageClicked = onImageClicked;
        this.imageWorker = new DrawableImageWorker(context, imageCache);
        imageWorker.setCacheEntryNameFactory(new CacheEntryNameFactory() {
            @Override
            @NonNull
            public String getCacheIndex(@NonNull Uri uri) {
                return uri.getPath().concat(".thumb");
            }
        });
        imageWorker.setLoaderFactory(SimpleLoaderFactory.RESULT_ON_MAIN_THREAD);
        final int thumbSize = (int) context.getResources().getDimension(R.dimen.history_thumb_size);
        imageWorker.setRequestedSize(thumbSize, thumbSize);
    }

    public void onDestroy() {
        imageWorker.setExitTasksEarly(true);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < numColumns) {
            return SEPARATOR_ENTRY;
        } else {
            return HISTORY_ENTRY;
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() + numColumns : 0;
    }

    @Override
    public HistoryEntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        switch (viewType) {
            case SEPARATOR_ENTRY:
                return HistoryEntryHolder.newEmptyItem(view);
            case HISTORY_ENTRY:
                return HistoryEntryHolder.newHistoryItem(view);
            default:
                throw new IllegalStateException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(HistoryEntryHolder holder, int position) {
        if (position < numColumns) return;
        position -= numColumns;
        cursor.moveToPosition(position);
        String uriAsString = cursor.getString(cursor.getColumnIndexOrThrow(OpenedImageDao.Properties.Uri.columnName));
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(OpenedImageDao.Properties.Date.columnName));
        final Uri uri = Uri.parse(uriAsString);
        File file = Utils.getRealPathFromURI(context, uri);
        imageWorker.loadImage(uri, holder.getImage());
        holder.getLastAccess().setText(DateFormat.getDateTimeInstance().format(new Date(timestamp)));
        holder.getImageName().setText(file.getName());
        holder.getMaterialRippleLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageClicked.openImage(uri);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {OpenedImageDao.Properties.Id.columnName, OpenedImageDao.Properties.Uri.columnName, OpenedImageDao.Properties.Date.columnName};
        String sortingOrder = OpenedImageDao.Properties.Date.columnName + " DESC";
        return new CursorLoader(context, OpenedImageContentProvider.CONTENT_URI, projection, null, null, sortingOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursor = data;
        notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursor = null;
        notifyDataSetChanged();
    }

    public interface OnImageClicked {
        void openImage(Uri uri);
    }
}
