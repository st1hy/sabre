package com.github.st1hy.sabre.image;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.github.st1hy.sabre.util.ImageCache;
import com.github.st1hy.sabre.util.ImageResizer;
import com.github.st1hy.sabre.util.ImageWorker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ImageViewer extends Viewer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageWorker imageWorker;
    private volatile Drawable background, image;
    private volatile ImageLoadingCallback loadingCallback;
    private final ImageWorker.ImageReceiver imageReceiver = new ImageReceiverImp();

    public ImageViewer(Context context) {
        super(context);
    }

    public ImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ImageViewer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();
    }

    public void addImageCache(ImageCache cache) {
        imageWorker = new ImageWorker(getContext(), cache);
        imageWorker.setImageFadeIn(false);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    public void setLoadingCallback(ImageLoadingCallback loadingCallback) {
        this.loadingCallback = loadingCallback;
    }

    public void setImageURI(final Uri uri) {
        if (loadingCallback != null) handler.post(new Runnable() {
            @Override
            public void run() {
                loadingCallback.onImageLoadingStarted();
            }
        });
        if (imageWorker != null) {
            imageWorker.loadImage(uri, imageReceiver);
        } else {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = ImageResizer.decodeUri(uri, Integer.MAX_VALUE, Integer.MAX_VALUE, null, getContext().getContentResolver());
                    imageReceiver.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                }
            });
        }
    }

    @Override
    public void drawContent(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        canvas.drawLine(0, 0, width, height, paint);
        drawDrawable(canvas, background);
        drawDrawable(canvas, image);
    }

    private void drawDrawable(Canvas canvas, Drawable drawable) {
        if (drawable == null) return;
        //configureBounds(drawable);
        int saveCount = canvas.getSaveCount();
        canvas.drawColor(Color.WHITE);
        canvas.save();
        Drawable current = drawable.getCurrent();
        configureBounds(canvas, drawable);
        current.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    private void configureBounds(Canvas canvas, Drawable drawable) {
        int dWidth = drawable.getIntrinsicWidth();
        int dHeight = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0, dWidth, dHeight);
        float scale = Math.min((float) width / (float) dWidth,
                (float) height / (float) dHeight);
        float dx = (int) ((width - dWidth * scale) * 0.5f + 0.5f);
        float dy = (int) ((height - dHeight * scale) * 0.5f + 0.5f);
        canvas.translate(dx, dy);
        canvas.scale(scale, scale);
    }

    public interface ImageLoadingCallback {
        void onImageLoadingStarted();

        void onImageLoadingFinished();
    }

    private class ImageReceiverImp implements ImageWorker.ImageReceiver, Drawable.Callback {
        private final Map<Drawable, Map<Runnable, Future<?>>> futureDrawingTasks = new ConcurrentHashMap<>();

        @Override
        public void setImageDrawable(Drawable drawable) {
            if (image != null) {
                image.setCallback(null);
                cancelAllFuture(image);
            }
            image = drawable;
            surfaceRedrawNeeded(holder);
            if (loadingCallback != null) handler.post(new Runnable() {
                @Override
                public void run() {
                    loadingCallback.onImageLoadingFinished();
                }
            });
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
        public Drawable getDrawable() {
            return image;
        }

        @Override
        public Drawable getBackground() {
            return background;
        }

        @Override
        public void invalidateDrawable(Drawable who) {
            surfaceRedrawNeeded(holder);
        }

        @Override
        public void scheduleDrawable(final Drawable who, final Runnable what, long when) {
            Map<Runnable, Future<?>> runnableFutureMap = futureDrawingTasks.get(who);
            if (runnableFutureMap == null) {
                runnableFutureMap = new ConcurrentHashMap<>();
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
}
