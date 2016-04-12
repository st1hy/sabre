package com.github.st1hy.sabre.history.utils;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class ForwardingLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
    private final LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;

    public ForwardingLoaderCallback(@NonNull LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks) {
        this.loaderCallbacks = loaderCallbacks;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return loaderCallbacks.onCreateLoader(id, args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        loaderCallbacks.onLoadFinished(loader, data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loaderCallbacks.onLoaderReset(loader);
    }
}
