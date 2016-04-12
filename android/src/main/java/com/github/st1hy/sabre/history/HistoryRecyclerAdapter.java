package com.github.st1hy.sabre.history;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.ImageView;

import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.dao.OpenedImageContentProvider;
import com.github.st1hy.sabre.dao.OpenedImageDao;
import com.github.st1hy.sabre.image.ImageActivity;
import com.google.common.base.Preconditions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindDimen;
import butterknife.ButterKnife;
import timber.log.BuildConfig;
import timber.log.Timber;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryEntryHolder> implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = HistoryRecyclerAdapter.class.getSimpleName();
    private static final int HISTORY_ENTRY = R.layout.history_entry_layout;

    private final Context context;

    private Cursor cursor;

    @BindDimen(R.dimen.history_thumb_size)
    int thumbSize;

    @Inject
    public HistoryRecyclerAdapter(@NonNull Activity context) {
        this.context = context;
        ButterKnife.bind(this, context);
    }

    public void onStop() {
        Picasso.with(context).cancelTag(TAG);
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
        Preconditions.checkNotNull(view);
        switch (viewType) {
            case HISTORY_ENTRY:
                return HistoryEntryHolder.newHistoryItem(view);
            default:
                throw new IllegalStateException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(final HistoryEntryHolder holder, int position) {
        cursor.moveToPosition(position);
        String uriAsString = cursor.getString(cursor.getColumnIndexOrThrow(OpenedImageDao.Properties.Uri.columnName));
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(OpenedImageDao.Properties.Date.columnName));
        String filename = cursor.getString(cursor.getColumnIndexOrThrow(OpenedImageDao.Properties.Filename.columnName));
        final Uri uri = Uri.parse(uriAsString);
        ImageView image = holder.getImage();
        Picasso.with(context)
                .cancelRequest(image);
        Picasso.with(context)
                .load(uri)
                .resize(thumbSize, thumbSize)
                .centerCrop()
                .onlyScaleDown()
                .tag(TAG)
                .into(image, new Callback.EmptyCallback() {

                    @Override
                    public void onError() {
                        if (BuildConfig.DEBUG) {
                            Timber.d("Error downloading: %s", uri);
                        }
                    }
                });
        holder.getLastAccess().setText(DateFormat.getDateTimeInstance().format(new Date(timestamp)));
        holder.getImageName().setText(filename != null ? filename : uriAsString);
        holder.getMaterialRippleLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage(uri);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {OpenedImageDao.Properties.Id.columnName, OpenedImageDao.Properties.Uri.columnName, OpenedImageDao.Properties.Date.columnName, OpenedImageDao.Properties.Filename.columnName};
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

    public void openImage(@NonNull Uri uri) {
        Intent intent = new Intent();
        intent.setDataAndTypeAndNormalize(uri, "image/*");
        intent.setAction(Intent.ACTION_VIEW);
        intent.setClass(context, ImageActivity.class);
        context.startActivity(intent);
    }
}
