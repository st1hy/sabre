package com.github.st1hy.sabre.image.gdx;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.github.st1hy.imagecache.CacheProvider;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.imagecache.worker.BitmapImageWorker;
import com.github.st1hy.imagecache.worker.ImageWorker;
import com.github.st1hy.imagecache.worker.SimpleLoaderFactory;
import com.github.st1hy.core.BackgroundColor;
import com.github.st1hy.core.ImageGdxCore;
import com.github.st1hy.core.ImageTexture;
import com.github.st1hy.gesturedetector.Config;
import com.github.st1hy.sabre.NavState;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.image.AsyncImageReceiver;
import com.github.st1hy.core.utils.MissingInterfaceException;
import com.github.st1hy.core.utils.UiThreadHandler;
import com.github.st1hy.core.utils.Utils;

public class GdxImageViewerFragment extends AndroidFragmentApplication implements AsyncImageReceiver.Callback {
    private static final String TAG = "GdxImageViewerFragment";
    private static final String STORE_MATRIX = "com.github.st1hy.sabre.transformation.matrix";

    private ImageGdxCore imageGdxCore;
    private GdxViewDelegate viewDelegate;
    private ImageWorker<Bitmap> imageWorker;
    private AsyncImageReceiver<Bitmap> imageReceiver = new BitmapImageReceiver(this);
    private ImageOnTouchListener imageOnTouchListener;
    private final UiThreadHandler handler = new UiThreadHandler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sanityCheck();
        this.imageGdxCore = new ImageGdxCore(getBackground());
        ImageCache imageCache = ((CacheProvider) getActivity()).getCacheHandler().getCache();
        imageWorker = new BitmapImageWorker(getActivity(), imageCache);
        imageWorker.setLoaderFactory(SimpleLoaderFactory.WITHOUT_DISK_CACHE);
        imageOnTouchListener = new ImageOnTouchListener(imageGdxCore);
        if (savedInstanceState!= null) {
            Object matrixSerialised = savedInstanceState.getSerializable(STORE_MATRIX);
            if (matrixSerialised instanceof float[]) {
                imageGdxCore.getTransformation().set((float[]) matrixSerialised);
            }
        }
    }

    private void sanityCheck() {
        MissingInterfaceException.parentSanityCheck(this, CacheProvider.class);
    }

    private BackgroundColor getBackground() {
        int color = Utils.getColor(getActivity(), R.color.image_surface_background);
        float r = Color.red(color) / 255f;
        float g = Color.green(color) / 255f;
        float b = Color.blue(color) / 255f;
        float a = Color.alpha(color) / 255f;
        return new BackgroundColor(r,g,b,a);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
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
        config.hideStatusBar = true;
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
    public void onResume() {
        super.onResume();
        imageWorker.setPauseWork(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        imageWorker.setPauseWork(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageWorker.setExitTasksEarly(true);
        imageWorker.cancelWork(imageReceiver);
        handler.removeAll();
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
    }
}
