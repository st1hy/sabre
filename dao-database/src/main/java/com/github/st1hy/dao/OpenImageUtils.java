package com.github.st1hy.dao;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.core.utils.Utils;

import java.io.File;
import java.util.Date;
import java.util.List;

public enum OpenImageUtils {
    ;

    public static void updateOpenedImage(@NonNull Context context, @NonNull DaoSession session, @NonNull Uri uri, @NonNull Date date) {
        OpenedImageDao dao = session.getOpenedImageDao();
        OpenedImage openedImage = getImage(dao, uri);
        if (openedImage == null) {
            File file = Utils.getRealPathFromURI(context, uri);
            String filename = file != null ? file.getName() : null;
            openedImage = new OpenedImage(null, uri.toString(), filename, date);
        } else {
            openedImage.setDate(date);
        }
        dao.insertOrReplace(openedImage);
        context.getContentResolver().notifyChange(OpenedImageContentProvider.CONTENT_URI, null);
    }

    @Nullable
    public static OpenedImage getImage(@NonNull OpenedImageDao dao, @NonNull Uri uri) {
        String uriString = uri.toString();
        List<OpenedImage> list = dao.queryBuilder().where(OpenedImageDao.Properties.Uri.eq(uriString)).build().list();
        if (list.size() > 0) {
            return list.iterator().next();
        } else {
            return null;
        }
    }
}
