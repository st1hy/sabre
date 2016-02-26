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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.github.st1hy.core.BackgroundColor;
import com.github.st1hy.core.ImageGdxCore;
import com.github.st1hy.core.ImageTexture;
import com.github.st1hy.core.utils.MissingInterfaceException;
import com.github.st1hy.core.utils.UiThreadHandler;
import com.github.st1hy.core.utils.Utils;
import com.github.st1hy.gesturedetector.Config;
import com.github.st1hy.imagecache.ImageCache;
import com.github.st1hy.imagecache.ImageCacheProvider;
import com.github.st1hy.imagecache.worker.ImageWorker;
import com.github.st1hy.imagecache.worker.ImageWorkerImp;
import com.github.st1hy.imagecache.worker.SimpleLoaderFactory;
import com.github.st1hy.imagecache.worker.creator.BitmapCreator;
import com.github.st1hy.sabre.NavState;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.image.AsyncImageReceiver;
import com.github.st1hy.sabre.image.ImageActivity;
import com.github.st1hy.sabre.image.gdx.touch.ImageTouchController;

import timber.log.Timber;

public class GdxImageViewerFragment extends AndroidFragmentApplication implements AsyncImageReceiver.Callback {
    private static final String STORE_MATRIX = "com.github.st1hy.sabre.transformation.matrix";

    private ImageGdxCore imageGdxCore;
    private GdxViewHolder viewHolder;
    private ImageWorker<Bitmap> imageWorker;
    private AsyncImageReceiver<Bitmap> imageReceiver = new BitmapImageReceiver(this);
    private ImageTouchController imageTouchController;
    private final UiThreadHandler handler = new UiThreadHandler();

    private Bitmap lastDrawn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sanityCheck();
        this.imageGdxCore = new ImageGdxCore(getBackground());
        imageTouchController = new ImageTouchController(imageGdxCore);
        if (savedInstanceState != null) {
            Object matrixSerialised = savedInstanceState.getSerializable(STORE_MATRIX);
            if (matrixSerialised instanceof float[]) {
                imageGdxCore.getTransformation().set((float[]) matrixSerialised);
            }
        }
    }

    private void sanityCheck() {
        MissingInterfaceException.parentSanityCheck(this, ImageCacheProvider.class);
    }

    private BackgroundColor getBackground() {
        int color = Utils.getColor(getActivity(), R.color.image_surface_background);
        float r = Color.red(color) / 255f;
        float g = Color.green(color) / 255f;
        float b = Color.blue(color) / 255f;
        float a = Color.alpha(color) / 255f;
        return new BackgroundColor(r, g, b, a);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        imageWorker = createWorker();
        Bundle arguments = getArguments();
        if (arguments != null) {
            Parcelable parcelable = arguments.getParcelable(NavState.ARG_IMAGE_URI);
            if (parcelable instanceof Uri) {
                setImageURI((Uri) parcelable);
            }
        }
    }


    private ImageWorker<Bitmap> createWorker() {
        ImageWorkerImp.Builder<Bitmap> builder = new ImageWorkerImp.Builder<>(getActivity(), new BitmapCreator());
        builder.setLoaderFactory(SimpleLoaderFactory.WITHOUT_DISK_CACHE);
        ImageCache imageCache = ((ImageCacheProvider) getActivity()).getImageCacheHandler().getCache();
        builder.setImageCache(imageCache);
        return builder.build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_image_gl, container, false);
        View glSurface = initializeForView(imageGdxCore, initConfig());
        glSurface.setOnTouchListener(imageTouchController);
        viewHolder = new GdxViewHolder(glSurface).bind(root);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder.unbind();
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
        imageTouchController.reset();
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

    @Override
    public void redrawNeeded() {
        final Bitmap bitmap = imageReceiver.getImage();
        if (bitmap == null) {
            if (Config.DEBUG) {
                Timber.w("Nothing to draw despite caller says otherwise!");
            }
            return;
        }
        if (Config.DEBUG) {
            Timber.v("Redrawing bitmap");
        }
        if (bitmap != lastDrawn) {
            lastDrawn = bitmap;
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (Config.DEBUG) {
                        Timber.v("Loading texture");
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

    @Override
    public void onImageLoadingFailed() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ImageActivity activity = (ImageActivity) getActivity();
                if (activity == null) return;
                activity.onImageFailedToLoad();
            }
        });
    }

    private void onLoadingStarted() {
        viewHolder.getLoadingProgressBar().setVisibility(View.VISIBLE);
        viewHolder.getGlSurfaceContainer().setVisibility(View.INVISIBLE);
    }

    private void onLoadingFinished() {
        viewHolder.getLoadingProgressBar().setVisibility(View.GONE);
        viewHolder.getGlSurfaceContainer().setVisibility(View.VISIBLE);
    }
}
