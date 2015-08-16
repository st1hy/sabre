package com.github.st1hy.sabre.image;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;

import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.GestureDetectorImp;
import com.github.st1hy.gesturedetector.GestureListener;
import com.github.st1hy.gesturedetector.SimpleGestureListener;
import com.github.st1hy.sabre.image.worker.ImageReceiver;
import com.github.st1hy.sabre.image.worker.ImageWorkerImp;
import com.github.st1hy.sabre.image.worker.ImageWorker;
import com.github.st1hy.sabre.image.worker.TaskOption;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ImageViewer extends Viewer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageWorker imageWorker;
    private volatile Drawable background, image;
    private volatile ImageLoadingCallback loadingCallback;
    private final ImageReceiver imageReceiver = new ImageReceiverImp();
    private GestureDetector gestureDetector;
    private GestureListener gestureListener;

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
        gestureListener = new SimpleGestureListener() {
            //TODO Respond to gestures.
        };
        gestureDetector = GestureDetectorImp.newInstance(getContext(), gestureListener, null);
        setOnTouchListener(gestureDetector);
    }

    public void addImageCache(ImageCache cache) {
        imageWorker = new ImageWorkerImp(getContext(), cache);
        imageWorker.setTaskOption(TaskOption.RUNNABLE);
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
        canvas.drawColor(Color.BLACK);
        drawDrawable(canvas, background);
        drawDrawable(canvas, image);
    }

    private void drawDrawable(Canvas canvas, Drawable drawable) {
        if (drawable == null) return;
        int saveCount = canvas.getSaveCount();
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

    private class ImageReceiverImp implements ImageReceiver, Drawable.Callback {
        private final Map<Drawable, Map<Runnable, Future<?>>> futureDrawingTasks = new ConcurrentHashMap<>();

        @Override
        public void setImageDrawable(Drawable drawable) {
            if (image != null) {
                image.setCallback(null);
                cancelAllFuture(image);
            }
            image = drawable;
            if (loadingCallback != null) handler.post(new Runnable() {
                @Override
                public void run() {
                    loadingCallback.onImageLoadingFinished();
                }
            });
            surfaceRedrawNeeded(holder);
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
