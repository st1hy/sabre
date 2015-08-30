package com.github.st1hy.sabre.image.surface;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;

import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.MatrixTransformationDetector;
import com.github.st1hy.gesturedetector.MultipleGestureListener;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.gesturedetector.SimpleGestureListener;
import com.github.st1hy.sabre.cache.ImageCache;
import com.github.st1hy.sabre.cache.ImageResizer;
import com.github.st1hy.sabre.surface.image.SurfaceViewer;
import com.github.st1hy.sabre.surface.image.ImageViewer;
import com.github.st1hy.sabre.image.bitmap.ImageReceiver;
import com.github.st1hy.sabre.image.bitmap.ImageWorker;
import com.github.st1hy.sabre.image.bitmap.ImageWorkerImp;
import com.github.st1hy.sabre.image.bitmap.TaskOption;
import com.github.st1hy.sabre.cache.worker.ImageReceiver;
import com.github.st1hy.sabre.cache.worker.ImageWorker;
import com.github.st1hy.sabre.cache.worker.ImageWorkerImp;
import com.github.st1hy.sabre.cache.worker.TaskOption;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ImageSurfaceViewer extends SurfaceViewer implements ImageViewer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageWorker imageWorker;
    private volatile Drawable background, image;
    private volatile ImageLoadingCallback loadingCallback;
    private final ImageReceiver imageReceiver = new ImageReceiverImp();
    private GestureDetector gestureDetector;
    private MultipleGestureListener gestureListener;

    public ImageSurfaceViewer(Context context) {
        super(context);
    }

    public ImageSurfaceViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageSurfaceViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ImageSurfaceViewer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private final Matrix matrix = new Matrix();

    @Override
    protected void init() {
        super.init();
        gestureListener = new SimpleGestureListener() {
            final Matrix startMatrix = new Matrix();

            @Override
            public void onMatrix(GestureEventState state, Matrix currentTransformation) {
                switch (state) {
                    case STARTED:
                        startMatrix.set(matrix);
                    case IN_PROGRESS:
                    case ENDED:
                        matrix.set(startMatrix);
                        matrix.postConcat(currentTransformation);
                        break;
                }
                surfaceRedrawNeeded(holder);
            }

            @Override
            public void onRotate(GestureEventState state, PointF centerPoint, double rotation, double delta) {
                matrix.postRotate((float) delta, centerPoint.x, centerPoint.y);
                surfaceRedrawNeeded(holder);
            }


            @Override
            public void onTranslate(GestureEventState state, PointF startPoint, float x, float y, float dx, float dy, double distance) {
                matrix.postTranslate(dx, dy);
                surfaceRedrawNeeded(holder);
            }

            @Override
            public void onScale(GestureEventState state, PointF centerPoint, float scale, float scaleRelative) {
                matrix.postScale(scaleRelative, scaleRelative, centerPoint.x, centerPoint.y);
                surfaceRedrawNeeded(holder);
            }
        };
        Options options = new Options(getResources());
        options.set(Options.Constant.MATRIX_MAX_POINTERS_COUNT, 2);
        gestureDetector = new MatrixTransformationDetector(gestureListener, options);
        setOnTouchListener(gestureDetector);
    }

    @Override
    public void addImageCache(ImageCache cache) {
        imageWorker = new ImageWorkerImp(getContext(), cache);
        imageWorker.setTaskOption(TaskOption.RUNNABLE);
    }

    @Override
    public void setLoadingCallback(ImageLoadingCallback loadingCallback) {
        this.loadingCallback = loadingCallback;
    }

    @Override
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
        canvas.setMatrix(matrix);
        //drawDrawable(canvas, background);
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

    @Override
    public void onPause() {
        gestureDetector.invalidate();
    }

    @Override
    public void onResume() {
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
