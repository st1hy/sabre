package com.github.st1hy.sabre.gdx.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.github.st1hy.core.ImageGdxCore;
import com.github.st1hy.core.ImageTexture;
import com.github.st1hy.gesturedetector.Config;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.sabre.cache.ImageCache;
import com.github.st1hy.sabre.cache.retainer.Retainer;
import com.github.st1hy.sabre.cache.retainer.SupportRetainFragment;
import com.github.st1hy.sabre.cache.worker.BitmapImageWorker;
import com.github.st1hy.sabre.cache.worker.ImageReceiver;
import com.github.st1hy.sabre.cache.worker.ImageWorker;
import com.github.st1hy.sabre.cache.worker.TaskOption;
import com.github.st1hy.sabre.util.DrawableImageReceiver;

public class ImageViewerFragment extends AndroidFragmentApplication implements ImageViewer, DrawableImageReceiver.Callback {
    private static final String TAG = "ImageViewerFragment";
    private Context context;
    private ImageGdxCore imageGdxCore;
    private View imageView;
    private ImageLoadingCallback loadingCallback;
    private ImageWorker<Bitmap> imageWorker;
    private ImageReceiver<Bitmap> imageReceiver = new BitmapImageReceiver(this);
    private ImageOnTouchListener imageOnTouchListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.imageGdxCore = new ImageGdxCore();
        imageWorker = new BitmapImageWorker(getActivity(), initCache());
        imageWorker.setTaskOption(TaskOption.RUNNABLE);
        imageOnTouchListener = new ImageOnTouchListener(imageGdxCore);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.imageView = initializeForView(imageGdxCore, initConfig());
        Options options = new Options();
        options.set(Options.Constant.MATRIX_MAX_POINTERS_COUNT, 3);
        options.setFlag(Options.Flag.MATRIX_OPEN_GL_COMPATIBILITY, true);
        options.setEnabled(Options.Event.MATRIX_TRANSFORMATION, true);
        imageView.setOnTouchListener(imageOnTouchListener);
        return imageView;
    }

    private ImageCache initCache() {
        ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams(context, "images");
        params.diskCacheEnabled = false;
        params.setMemCacheSizePercent(0.25f);
        Retainer retainer = SupportRetainFragment.findOrCreateRetainFragment(getFragmentManager());
        final ImageCache imageCache = ImageCache.getInstance(retainer, params);
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

//    private View initView() {
//        View view =
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        view.setLayoutParams(layoutParams);
//        return view;
//    }


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
        handler.post(new Runnable() {
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
