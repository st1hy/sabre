package com.github.st1hy.sabre.settings;

import android.support.annotation.NonNull;
import android.view.View;

import com.github.st1hy.core.utils.Utils;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.imagecache.ImageCacheHandler;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.injector.ViewBinder;

class ClearCacheHolder extends SettingOnOffHolder implements View.OnClickListener {
    private final ImageCacheHandler imageCacheHandler;

    public ClearCacheHolder(@NonNull ImageCacheHandler imageCacheHandler) {
        this.imageCacheHandler = imageCacheHandler;
    }

    @Override
    public ViewBinder bind(@NonNull View view) {
        super.bind(view);
        setTitle(R.string.pref_clear_cache_title);
        setSubTitle(R.string.pref_clear_cache_desc);
        getCheckBox().setVisibility(View.GONE);
        view.setOnClickListener(this);
        return this;
    }

    @Override
    public void onClick(View v) {
        final ImageCache cache = imageCacheHandler.getCache();
        Utils.CACHED_EXECUTOR_POOL.execute(new Runnable() {
            @Override
            public void run() {
                cache.clearCache();
            }
        });
    }
}
