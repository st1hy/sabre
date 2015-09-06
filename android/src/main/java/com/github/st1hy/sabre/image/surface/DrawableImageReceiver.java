package com.github.st1hy.sabre.image.surface;

import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import com.github.st1hy.sabre.core.cache.worker.ImageReceiver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DrawableImageReceiver implements ImageReceiver<Drawable>, Drawable.Callback {
    private Drawable background, image;
    private final Map<Drawable, Map<Runnable, Future<?>>> futureDrawingTasks = new HashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Callback callback;

    public DrawableImageReceiver(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setImage(Drawable drawable) {
        if (image != null) {
            image.setCallback(null);
            cancelAllFuture(image);
        }
        image = drawable;
        callback.onImageLoaded();
        callback.redrawNeeded();
        if (image == null) return;
        image.setCallback(this);
    }

    @Override
    public void setBackground(Drawable drawable) {
        if (background != null) {
            background.setCallback(null);
            cancelAllFuture(background);
        }
        background = drawable;
        if (background == null) return;
        background.setCallback(this);
    }

    private void cancelAllFuture(Drawable drawable) {
        Map<Runnable, Future<?>> runnableFutureMap = futureDrawingTasks.get(drawable);
        if (runnableFutureMap == null) return;
        for (Map.Entry<Runnable, Future<?>> entry : runnableFutureMap.entrySet()) {
            entry.getValue().cancel(true);
        }
        futureDrawingTasks.remove(drawable);
    }

    @Override
    public Drawable getImage() {
        return image;
    }

    @Override
    public Drawable getBackground() {
        return background;
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        callback.redrawNeeded();
    }

    @Override
    public void scheduleDrawable(final Drawable who, final Runnable what, long when) {
        Map<Runnable, Future<?>> runnableFutureMap = futureDrawingTasks.get(who);
        if (runnableFutureMap == null) {
            runnableFutureMap = new HashMap<>();
            futureDrawingTasks.put(who, runnableFutureMap);
        }
        Future<?> future = executor.schedule(new Runnable() {
            @Override
            public void run() {
                Map<Runnable, Future<?>> runnableFutureMap = futureDrawingTasks.get(who);
                if (runnableFutureMap != null) {
                    runnableFutureMap.remove(what);
                }
                what.run();
            }
        }, when - SystemClock.uptimeMillis(), TimeUnit.MILLISECONDS);
        runnableFutureMap.put(what, future);
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        Map<Runnable, Future<?>> runnableFutureMap = futureDrawingTasks.get(who);
        if (runnableFutureMap == null) return;
        Future<?> future = runnableFutureMap.get(what);
        if (future == null) return;
        future.cancel(true);
    }
}
