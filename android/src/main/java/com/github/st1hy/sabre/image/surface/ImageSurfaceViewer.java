package com.github.st1hy.sabre.image.surface;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;

import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.MatrixTransformationDetector;
import com.github.st1hy.gesturedetector.MultipleGestureListener;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.gesturedetector.SimpleGestureListener;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.cache.ImageCache;
import com.github.st1hy.sabre.core.cache.ImageResizer;
import com.github.st1hy.sabre.core.cache.worker.DrawableImageWorker;
import com.github.st1hy.sabre.core.cache.worker.ImageWorker;
import com.github.st1hy.sabre.core.cache.worker.SimpleLoaderFactory;
import com.github.st1hy.sabre.core.util.UiThreadHandler;
import com.github.st1hy.sabre.core.util.Utils;
import com.github.st1hy.sabre.image.AsyncImageReceiver;

public class ImageSurfaceViewer extends SurfaceViewer implements ImageViewer, AsyncImageReceiver.Callback {
    private ImageWorker<Drawable> imageWorker;
    private volatile ImageLoadingCallback loadingCallback;
    private final AsyncImageReceiver<Drawable> imageReceiver = new DrawableImageReceiver(this);
    private GestureDetector gestureDetector;
    private int backgroundColor;
    private final UiThreadHandler handler = new UiThreadHandler();

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
        MultipleGestureListener gestureListener = new SimpleGestureListener() {
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
        };
        Options options = new Options(getResources());
        options.set(Options.Constant.MATRIX_MAX_POINTERS_COUNT, 2);
        gestureDetector = new MatrixTransformationDetector(gestureListener, options);
        setOnTouchListener(gestureDetector);
        backgroundColor = Utils.getColor(getContext(), R.color.image_surface_background);
    }

    @Override
    public void addImageCache(ImageCache cache) {
        imageWorker = new DrawableImageWorker(getContext(), cache);
        imageWorker.setLoaderFactory(SimpleLoaderFactory.WITHOUT_DISK_CACHE);
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
                    imageReceiver.setImage(new BitmapDrawable(getResources(), bitmap));
                }
            });
        }
    }

    @Override
    public void drawContent(Canvas canvas) {
        canvas.drawColor(backgroundColor);
        canvas.setMatrix(matrix);
        //drawDrawable(canvas, background);
        drawDrawable(canvas, imageReceiver.getImage());
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

    @Override
    public void onDestroy() {
        if (imageWorker != null) {
            imageWorker.setExitTasksEarly(true);
            imageWorker.cancelWork(imageReceiver);
        }
        handler.removeAll();
    }

    @Override
    public void onImageLoaded() {
        if (loadingCallback != null) handler.post(new Runnable() {
            @Override
            public void run() {
                loadingCallback.onImageLoadingFinished();
            }
        });
    }

    @Override
    public void redrawNeeded() {
        surfaceRedrawNeeded(holder);
    }
}
