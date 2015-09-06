package com.github.st1hy.sabre.image.gdx;

import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.github.st1hy.core.ImageGdxCore;
import com.github.st1hy.core.ImageTexture;
import com.github.st1hy.gesturedetector.Config;
import com.github.st1hy.sabre.MainActivity;
import com.github.st1hy.sabre.NavState;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.core.cache.ImageCache;
import com.github.st1hy.sabre.core.cache.worker.BitmapImageWorker;
import com.github.st1hy.sabre.core.cache.worker.ImageReceiver;
import com.github.st1hy.sabre.core.cache.worker.ImageWorker;
import com.github.st1hy.sabre.core.cache.worker.TaskOption;
import com.github.st1hy.sabre.core.util.SystemUIMode;

public class GdxImageViewerFragment extends AndroidFragmentApplication implements ImageReceiver.Callback {
    private static final String TAG = "GdxImageViewerFragment";
    private static final String STORE_MATRIX = "com.github.st1hy.sabre.transformation.matrix";
//    private static final String STORE_IMAGE_URI = "com.github.st1hy.sabre.IMAGE_URI";

    private ImageGdxCore imageGdxCore;
    private GdxViewDelegate viewDelegate;
    private ImageWorker<Bitmap> imageWorker;
    private ImageReceiver<Bitmap> imageReceiver = new BitmapImageReceiver(this);
    private ImageOnTouchListener imageOnTouchListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.imageGdxCore = new ImageGdxCore();
        ImageCache imageCache = ((MainActivity) getActivity()).getDependencyDelegate().getCacheHandler().getCache();
        imageWorker = new BitmapImageWorker(getActivity(), imageCache);
        imageWorker.setTaskOption(TaskOption.RUNNABLE);
        imageOnTouchListener = new ImageOnTouchListener(imageGdxCore);
        if (savedInstanceState!= null) {
            Object matrixSerialied = savedInstanceState.getSerializable(STORE_MATRIX);
            if (matrixSerialied instanceof float[]) {
                imageGdxCore.getTransformation().set((float[]) matrixSerialied);
            }
        }
        SystemUIMode.LAYOUT_FULLSCREEN.apply(getApplicationWindow());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_image_gl, container, false);
        View glSurface = initializeForView(imageGdxCore, initConfig());
        glSurface.setOnTouchListener(imageOnTouchListener);
        viewDelegate = new GdxViewDelegate(root, glSurface);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            Parcelable parcelable = arguments.getParcelable(NavState.ARG_IMAGE_URI);
            if (parcelable instanceof Uri) {
                setImageURI((Uri) parcelable);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STORE_MATRIX, imageGdxCore.getTransformation().getValues());
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

    public void setImageURI(final Uri uri) {
        onLoadingStarted();
        imageWorker.loadImage(uri, imageReceiver);
        imageOnTouchListener.reset();
    }

    @Override
    public void onImageLoaded() {
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
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onLoadingFinished();
                        }
                    });
                }
            });
        }
    }

    private void onLoadingStarted() {
        viewDelegate.getLoadingProgressBar().setVisibility(View.VISIBLE);
        viewDelegate.getGlSurfaceContainer().setVisibility(View.INVISIBLE);
    }

    private void onLoadingFinished() {
        viewDelegate.getLoadingProgressBar().setVisibility(View.GONE);
        viewDelegate.getGlSurfaceContainer().setVisibility(View.VISIBLE);
        SystemUIMode.IMMERSIVE.apply(getApplicationWindow());
        getApplicationWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getApplicationWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
