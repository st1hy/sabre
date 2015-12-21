package com.github.st1hy.dao;

import android.content.Context;
import android.net.Uri;

import java.util.Date;
import java.util.List;

public enum OpenImageUtils {
    ;

    public static void updateOpenedImage(Context context, DaoSession session, Uri uri, Date date) {
        OpenedImageDao dao = session.getOpenedImageDao();
        OpenedImage openedImage = getImage(dao, uri);
        openedImage.setDate(date);
        dao.insertOrReplace(openedImage);
        context.getContentResolver().notifyChange(OpenedImageContentProvider.CONTENT_URI, null);
    }

    public static OpenedImage getImage(OpenedImageDao dao, Uri uri) {
        String uriString = uri.toString();
        List<OpenedImage> list = dao.queryBuilder().where(OpenedImageDao.Properties.Uri.eq(uriString)).build().list();
        if (list.size() > 0) {
            return list.iterator().next();
        } else {
            return new OpenedImage(null, uriString, null);
        }
    }
}
