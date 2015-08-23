package com.github.st1hy.sabre.image.surface;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SurfaceViewer extends SurfaceView implements SurfaceHolder.Callback2 {
    protected volatile ScheduledExecutorService executor = newExecutor();
    protected volatile SurfaceHolder holder;
    protected volatile int width, height;
    protected volatile Bitmap cache;
    protected volatile Canvas cacheCanvas;
    private volatile DrawTask drawTask;
    private volatile boolean isSurfaceActive = false;

    public SurfaceViewer(Context context) {
        super(context);
        init();
    }

    public SurfaceViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SurfaceViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(22)
    public SurfaceViewer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        drawTask = new DrawTask();
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        drawTask.onRedrawNeeded();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceActive = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.width = width;
        this.height = height;
        cache = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas(cache);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceActive = false;
        drawTask.shutdownNow();
    }

    private class DrawTask implements Runnable {
        private final AtomicInteger pending = new AtomicInteger(0);

        @Override
        public void run() {
            pending.decrementAndGet();
            if (isInterrupted()) return;
            if (width == 0 || height == 0 || !isSurfaceActive) return;
            Canvas canvas = holder.lockCanvas();
            if (!isInterrupted()) {
                drawContent(canvas);
            }
            holder.unlockCanvasAndPost(canvas);
        }

        void onRedrawNeeded() {
            if (executor.isShutdown()) {
                executor = newExecutor();
            }
            if (pending.compareAndSet(0, 1)) {
                executor.submit(this);
            }
        }

        void shutdownNow() {
            executor.shutdownNow();
            boolean isTerminated = executor.isTerminated();
            if (!isTerminated) {
                try {
                    executor.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    public abstract void drawContent(Canvas canvas);

    private static ScheduledExecutorService newExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
