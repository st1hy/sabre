package com.github.st1hy.sabre.core.injector.module;

import android.content.ContentProviderClient;
import android.content.Context;
import android.support.annotation.NonNull;

import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.dao.DaggerOpenedImageContentProvider;
import com.github.st1hy.sabre.dao.DaoSession;
import com.google.common.base.Preconditions;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    private final Application app;

    public ApplicationModule(@NonNull Application app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return app.getApplicationContext();
    }

    @Provides
    @Singleton
    DaoSession provideSession() {
        ContentProviderClient contentProviderClient = app.getApplicationContext()
                .getContentResolver()
                .acquireContentProviderClient(DaggerOpenedImageContentProvider.CONTENT_URI);
        Preconditions.checkNotNull(contentProviderClient);
        DaggerOpenedImageContentProvider contentProvider = (DaggerOpenedImageContentProvider) contentProviderClient.getLocalContentProvider();
        Preconditions.checkNotNull(contentProvider);
        return contentProvider.getSession();
    }
}
