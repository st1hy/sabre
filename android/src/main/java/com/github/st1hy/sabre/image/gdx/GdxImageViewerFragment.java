package com.github.st1hy.sabre.image.gdx;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.github.st1hy.core.utils.MissingInterfaceException;
import com.github.st1hy.core.utils.Utils;
import com.github.st1hy.gesturedetector.Config;
import com.github.st1hy.sabre.BuildConfig;
import com.github.st1hy.sabre.R;
import com.github.st1hy.sabre.image.ImageActivity;
import com.github.st1hy.sabre.image.gdx.touch.ImageTouchController;
import com.github.st1hy.sabre.libgdx.ImageGdxCore;
import com.github.st1hy.sabre.libgdx.ImageScreen;
import com.github.st1hy.sabre.libgdx.ScreenContext;
import com.squareup.picasso.Picasso;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.concurrency.GdxScheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.picasso.BitmapLoadedEvent;
import rx.picasso.RxTarget;
import rx.picasso.TargetEvent;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class GdxImageViewerFragment extends AndroidFragmentApplication  {
    private static final String TAG = GdxImageViewerFragment.class.getSimpleName();

    private ImageGdxCore imageGdxCore;
    private GdxViewHolder viewHolder;
    private ImageTouchController imageTouchController;

    private static final String STORE_SCREEN_CONTEXT_MODEL = "Screen_model";
    private String screenContextJson = null;
    private ScreenContext screenContext;

    private CompositeSubscription subscriptions = new CompositeSubscription();
    private RxTarget rxTarget;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sanityCheck();
        this.imageGdxCore = new ImageGdxCore(getBackground());
        imageTouchController = new ImageTouchController(getActivity());
        if (BuildConfig.DEBUG) setLogLevel(LOG_DEBUG);
        rxTarget = RxTarget.get();
    }

    private void sanityCheck() {
        MissingInterfaceException.parentSanityCheck(this, ImageActivity.class);
    }

    private com.badlogic.gdx.graphics.Color getBackground() {
        int color = Utils.getColor(getActivity(), R.color.image_surface_background);
        float r = Color.red(color) / 255f;
        float g = Color.green(color) / 255f;
        float b = Color.blue(color) / 255f;
        float a = Color.alpha(color) / 255f;
        return new com.badlogic.gdx.graphics.Color(r, g, b, a);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageActivity activity = (ImageActivity) getActivity();
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if (savedInstanceState != null) {
            screenContextJson = savedInstanceState.getString(STORE_SCREEN_CONTEXT_MODEL);
        }
        Uri uri = activity.getImageUriFromIntent();
        if (uri != null) {
            setImageURI(uri);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (screenContext != null) {
            outState.putString(STORE_SCREEN_CONTEXT_MODEL, screenContext.toJson());
        }
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
    public void onStop() {
        super.onStop();
        subscriptions.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder.unbind();
        imageTouchController.invalidate();
        Picasso.with(getActivity())
                .cancelTag(TAG);
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

    public void setImageURI(@NonNull final Uri uri) {
        Observable<TargetEvent> observableTarget = rxTarget.toObservable();
        subscriptions.add(observableTarget.filter(
                new Func1<TargetEvent, Boolean>() {
                    @Override
                    public Boolean call(TargetEvent event) {
                        return event.getType() == TargetEvent.BitmapEventType.LOADED;
                    }
                })
                .observeOn(GdxScheduler.get())
                .map(new Func1<TargetEvent, ImageScreen>() {
                    @Override
                    public ImageScreen call(TargetEvent event) {
                        BitmapLoadedEvent bitmapLoaded = (BitmapLoadedEvent) event.getEvent();
                        Bitmap bitmap = bitmapLoaded.getBitmap();
                        if (Config.DEBUG) {
                            Timber.v("Loading texture");
                        }
                        Texture tex = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex.getTextureObjectHandle());
                        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                        screenContext = ScreenContext.createScreenContext(tex, screenContextJson);
                        return imageGdxCore.setImage(screenContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ImageScreen>() {
                    @Override
                    public void call(ImageScreen imageScreen) {
                        imageTouchController.setDispatch(imageScreen,
                                imageScreen.getPathDrawingListener(),
                                imageScreen.getImageFragmentSelector());
                        onLoadingFinished();
                    }
                }));
        subscriptions.add(observableTarget.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<TargetEvent>() {
                    @Override
                    public void call(TargetEvent event) {
                        switch (event.getType()) {
                            case PREPARED:
                                onLoadingStarted();
                                imageTouchController.resetViewPort();
                                break;
                            case FAILED:
                                ImageActivity activity = (ImageActivity) getActivity();
                                activity.onImageFailedToLoad();
                                break;
                            case LOADED:
                                break;
                        }
                    }
                }));
        Picasso.with(getActivity())
                .load(uri)
                .tag(TAG)
                .into(rxTarget);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageTouchController.onDestroy();
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
