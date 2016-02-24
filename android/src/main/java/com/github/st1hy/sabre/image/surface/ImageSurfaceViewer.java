package com.github.st1hy.sabre.image.surface;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import com.github.st1hy.core.utils.UiThreadHandler;
import com.github.st1hy.core.utils.Utils;
import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.MatrixTransformationDetector;
import com.github.st1hy.gesturedetector.MultipleGestureListener;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.gesturedetector.SimpleGestureListener;
import com.github.st1hy.imagecache.BitmapProvider;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.imagecache.decoder.UriBitmapFactory;
import com.github.st1hy.imagecache.decoder.UriBitmapSource;
import com.github.st1hy.imagecache.resize.KeepOriginal;
import com.github.st1hy.imagecache.worker.AbstractImageWorker;
import com.github.st1hy.imagecache.worker.ImageWorker;
import com.github.st1hy.imagecache.worker.SimpleLoaderFactory;
import com.github.st1hy.imagecache.worker.creator.DrawableCreator;
import com.github.st1hy.sabre.Application;
import com.github.st1hy.sabre.R;
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
        Context context = getContext();
        DrawableCreator drawableCreator = new DrawableCreator(context.getResources());
        AbstractImageWorker.Builder<Drawable> builder = new AbstractImageWorker.Builder<>(context, drawableCreator);
        builder.setLoaderFactory(SimpleLoaderFactory.WITHOUT_DISK_CACHE);
        builder.setImageCache(cache);
        imageWorker = builder.build();
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
            Application.CACHED_EXECUTOR_POOL.execute(new Runnable() {
                private BitmapProvider<UriBitmapSource> bitmapProvider;

                @Override
                public void run() {
                    Bitmap bitmap = getBitmapProvider().getImage(UriBitmapSource.of(getContext().getContentResolver() , uri));
                    imageReceiver.setImage(new BitmapDrawable(getResources(), bitmap));
                }

                private BitmapProvider<UriBitmapSource> getBitmapProvider() {
                    if (bitmapProvider == null) {
                        BitmapProvider.Builder<UriBitmapSource> builder = new BitmapProvider.Builder<>(new UriBitmapFactory());
                        builder.setResizingStrategy(new KeepOriginal());
                        bitmapProvider = builder.build();
                    }
                    return bitmapProvider;
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
