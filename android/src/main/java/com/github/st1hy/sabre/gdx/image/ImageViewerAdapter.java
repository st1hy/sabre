package com.github.st1hy.sabre.gdx.image;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix3;
import com.github.st1hy.core.ImageGdxCore;
import com.github.st1hy.core.ImageTexture;
import com.github.st1hy.gesturedetector.Config;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.MatrixTransformationDetector;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.gesturedetector.SimpleGestureListener;
import com.github.st1hy.sabre.cache.ImageCache;
import com.github.st1hy.sabre.cache.worker.BitmapImageWorker;
import com.github.st1hy.sabre.cache.worker.ImageReceiver;
import com.github.st1hy.sabre.cache.worker.ImageWorker;
import com.github.st1hy.sabre.cache.worker.TaskOption;
import com.github.st1hy.sabre.util.DrawableImageReceiver;

public class ImageViewerAdapter implements ImageViewer, DrawableImageReceiver.Callback {
    private static final String TAG = "ImageViewerAdapter";
    private final ImageGdxCore imageGdxCore;
    private final View imageView;
    private ImageLoadingCallback loadingCallback;
    private final AndroidApplication activity;
    private final ImageWorker<Bitmap> imageWorker;
    private final ImageReceiver<Bitmap> imageReceiver = new BitmapImageReceiver(this);

    public ImageViewerAdapter(AndroidApplication activity, ViewGroup viewContainer) {
        this.activity = activity;
        this.imageGdxCore = new ImageGdxCore();
        this.imageView = initView(viewContainer);
        imageWorker = new BitmapImageWorker(activity, initCache());
        imageWorker.setTaskOption(TaskOption.RUNNABLE);
        Options options = new Options();
        options.set(Options.Constant.MATRIX_MAX_POINTERS_COUNT, 3);
        options.setFlag(Options.Flag.MATRIX_OPEN_GL_COMPATIBILITY, true);
        options.setEnabled(Options.Event.MATRIX_TRANSFORMATION, true);
        imageView.setOnTouchListener(new MatrixTransformationDetector(new SimpleGestureListener() {
            float[] valuesTemp = new float[9];
            float[] valuesTempColumnMajor = new float[9];
            Matrix3 startingMatrix = new Matrix3();
            Matrix3 matrix3Temp = new Matrix3();
            Matrix3 matrix3Multiplied = new Matrix3();

            @Override
            public void onMatrix(GestureEventState gestureEventState, Matrix matrix) {
                if (gestureEventState.equals(GestureEventState.STARTED)) {
                    startingMatrix.set(matrix3Multiplied);
                }
                matrix.getValues(valuesTemp);
                changeMemoryOrder(valuesTemp, valuesTempColumnMajor);
                matrix3Temp.set(valuesTempColumnMajor);
                matrix3Multiplied.set(matrix3Temp);
                matrix3Multiplied.mul(startingMatrix);
                imageGdxCore.getTransformation().set(matrix3Multiplied);
                Gdx.graphics.requestRendering();
            }

        }, options));
    }

    private static void changeMemoryOrder(float[] rowMajorInput, float[] columnMajorOutput) {
        columnMajorOutput[0] = rowMajorInput[0];
        columnMajorOutput[1] = rowMajorInput[3];
        columnMajorOutput[2] = rowMajorInput[6];
        columnMajorOutput[3] = rowMajorInput[1];
        columnMajorOutput[4] = rowMajorInput[4];
        columnMajorOutput[5] = rowMajorInput[7];
        columnMajorOutput[6] = rowMajorInput[2];
        columnMajorOutput[7] = rowMajorInput[5];
        columnMajorOutput[8] = rowMajorInput[8];
    }

    private ImageCache initCache() {
        ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams(activity, "images");
        params.diskCacheEnabled = false;
        params.setMemCacheSizePercent(0.25f);
        final ImageCache imageCache = ImageCache.getInstance(activity.getFragmentManager(), params);
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                imageCache.initDiskCache();
            }
        });
        return imageCache;
    }

    private AndroidApplicationConfiguration initConfig() {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.disableAudio = true;
        config.hideStatusBar = false;
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGLSurfaceView20API18 = false;
//        config.useImmersiveMode = true;
        return config;
    }

    private View initView(ViewGroup viewContainer) {
        View view = activity.initializeForView(imageGdxCore, initConfig());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(layoutParams);
        viewContainer.addView(view);
        return view;
    }


    @Override
    public void setLoadingCallback(ImageLoadingCallback loadingCallback) {
        this.loadingCallback = loadingCallback;
    }

    @Override
    public void setImageURI(final Uri uri) {
        if (loadingCallback != null) loadingCallback.onImageLoadingStarted();
        imageWorker.loadImage(uri, imageReceiver);
    }

    @Override
    public void setVisibility(int visibility) {
        imageView.setVisibility(visibility);
    }

    @Override
    public void onImageLoaded() {
        activity.handler.post(new Runnable() {
            @Override
            public void run() {
                loadingCallback.onImageLoadingFinished();
            }
        });
    }

    private Bitmap lastDrawn;

    @Override
    public void redrawNeeded() {
        final Bitmap bitmap = imageReceiver.getImage();
        if (bitmap == null) {
            if (Config.DEBUG) {
                Log.e(TAG, "Nothing to draw despite caller says otherwise!");
            }
            return;
        }
        if (Config.DEBUG) {
            Log.v(TAG, "Redrawing bitmap");
        }
        if (bitmap != lastDrawn) {
            lastDrawn = bitmap;
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (Config.DEBUG) {
                        Log.v(TAG, "Loading texture");
                    }
                    Texture tex = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex.getTextureObjectHandle());
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                    imageGdxCore.loadTexture(new ImageTexture(tex));
                }
            });
        }
    }
}
