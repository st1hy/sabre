package com.github.st1hy.sabre.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class Viewer extends SurfaceView implements SurfaceHolder.Callback2, Runnable {
    protected ExecutorService executor = Executors.newSingleThreadExecutor();
    protected SurfaceHolder holder;
    protected int width, height;
    protected volatile Bitmap cache;
    protected volatile Canvas cacheCanvas;

    public Viewer(Context context) {
        super(context);
        init();
    }

    public Viewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Viewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(22)
    public Viewer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        if (executor.isShutdown())
            executor = Executors.newSingleThreadExecutor();
        executor.submit(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
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

    @Override
    public void run() {
        Canvas canvas = holder.lockCanvas();
        drawContent(canvas);
        holder.unlockCanvasAndPost(canvas);
    }

    public abstract void drawContent(Canvas canvas);
}
