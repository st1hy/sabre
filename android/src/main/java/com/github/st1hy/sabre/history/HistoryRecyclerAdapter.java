package com.github.st1hy.sabre.history;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.github.st1hy.imagecache.worker.AbstractImageWorker;
import com.github.st1hy.imagecache.worker.ImageWorker;
import com.github.st1hy.imagecache.worker.SimpleLoaderFactory;
import com.github.st1hy.imagecache.worker.creator.DrawableCreator;
import com.github.st1hy.imagecache.worker.name.CacheEntryNameFactory;
import com.github.st1hy.sabre.R;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryEntryHolder> implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int HISTORY_ENTRY = R.layout.history_entry_layout;

    private final Context context;
    private final ImageWorker<Drawable> imageWorker;
    private final OnImageClicked onImageClicked;
    private Cursor cursor;

    public HistoryRecyclerAdapter(@NonNull Context context, ImageCache imageCache, OnImageClicked onImageClicked) {
        this.context = context;
        this.onImageClicked = onImageClicked;
        this.imageWorker = createWorker(context, imageCache);
    }

    private static ImageWorker<Drawable> createWorker(@NonNull Context context, @Nullable ImageCache imageCache) {
        Resources resources = context.getResources();
        DrawableCreator drawableCreator = new DrawableCreator(resources);
        AbstractImageWorker.Builder<Drawable> builder = new AbstractImageWorker.Builder<>(context, drawableCreator);
        builder.setLoaderFactory(SimpleLoaderFactory.RESULT_ON_MAIN_THREAD);
        builder.setImageCache(imageCache);
        final int thumbSize = resources.getDimensionPixelSize(R.dimen.history_thumb_size);
        builder.setRequestedSize(thumbSize, thumbSize);
        builder.setCacheEntryNameFactory(new CacheEntryNameFactory() {
            @Override
            @NonNull
            public String getCacheIndex(@NonNull Uri uri) {
                return uri.getPath().concat(".thumb");
            }
        });
        return builder.build();
    }

    public void onDestroy() {
        imageWorker.setExitTasksEarly(true);
    }

    @Override
    public int getItemViewType(int position) {
        return HISTORY_ENTRY;
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    @Override
    public HistoryEntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        switch (viewType) {
            case HISTORY_ENTRY:
                return HistoryEntryHolder.newHistoryItem(view);
            default:
                throw new IllegalStateException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(HistoryEntryHolder holder, int position) {
        cursor.moveToPosition(position);
        String uriAsString = cursor.getString(cursor.getColumnIndexOrThrow(OpenedImageDao.Properties.Uri.columnName));
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(OpenedImageDao.Properties.Date.columnName));
        final Uri uri = Uri.parse(uriAsString);
        File file = Utils.getRealPathFromURI(context, uri);
        imageWorker.loadImage(uri, holder.getImage());
        holder.getLastAccess().setText(DateFormat.getDateTimeInstance().format(new Date(timestamp)));
        holder.getImageName().setText(file.getName());//TODO Remove non existing items from database. Fix potential nullptr exception.
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
