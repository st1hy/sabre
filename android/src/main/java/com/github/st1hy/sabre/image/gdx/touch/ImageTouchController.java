package com.github.st1hy.sabre.image.gdx.touch;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import com.github.st1hy.coregdx.OnPathChangedListener;
import com.github.st1hy.coregdx.Transformable;
import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.MultipleGestureDetector;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.gesturedetector.SimpleGestureListener;
import com.github.st1hy.sabre.libgdx.fragments.ImageFragmentSelector;
import com.github.st1hy.sabre.libgdx.mode.UiMode;
import com.github.st1hy.view.SelectorOnTouchListener;
import com.github.st1hy.view.SimpleTouchPredicate;
import com.github.st1hy.view.TouchPredicate;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ImageTouchController extends SimpleGestureListener implements GestureDetector {
    private final AndroidToLibGdxMatrixAdapter adapter;
    private final PathGestureDetector pathGestureDetector;
    private final SelectorOnTouchListener<GestureDetector> delegate;
    private final Options options;
    private ImageFragmentSelector imageFragmentSelector = null;
    private final ModePredicate selectElementPredicate;
    private Subscription uiModeSubscribtion;

    public ImageTouchController(@NonNull Context context) {
        this.options = setupOptions(context);
        adapter = new AndroidToLibGdxMatrixAdapter();
        pathGestureDetector = new PathGestureDetector(options);
        selectElementPredicate = new ModePredicate(UiMode.CUT_ELEMENT);
        delegate = buildSelector();
        uiModeSubscribtion = UiMode.toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UiMode>() {
            @Override
            public void call(UiMode uiMode) {
                selectElementPredicate.onUiModeChanged(uiMode);
            }
        });
    }

    public void setDispatch(@Nullable Transformable screen,
                            @Nullable OnPathChangedListener pathDrawingListener,
                            @Nullable ImageFragmentSelector imageFragmentSelector) {
        adapter.setDispatch(screen);
        pathGestureDetector.setListener(pathDrawingListener);
        this.imageFragmentSelector = imageFragmentSelector;
    }

    public void onDestroy() {
        uiModeSubscribtion.unsubscribe();
    }

    @NonNull
    private SelectorOnTouchListener<GestureDetector> buildSelector() {
        Map<TouchPredicate, GestureDetector> conditionMap = new HashMap<>();

        TouchPredicate deadZone = new DeadZoneTouchPredicate();
        conditionMap.put(deadZone, null);

        conditionMap.put(selectElementPredicate, new MultipleGestureDetector(this, options));

        TouchPredicate allElse = new SimpleTouchPredicate(true);
        conditionMap.put(allElse, buildMatrixTransformationDetector());

        List<TouchPredicate> order = Lists.newArrayList(deadZone, selectElementPredicate, allElse);
        return new SelectorOnTouchListener<>(conditionMap, order);
    }

    private static Options setupOptions(Context context) {
        Options options = new Options(context.getResources());
        for (Options.Event event : Options.Event.values()) {
            options.setEnabled(event, false);
        }
        options.setFlag(Options.Flag.MATRIX_OPEN_GL_COMPATIBILITY, true);
        options.setEnabled(Options.Event.TRANSLATE, true);
        options.setEnabled(Options.Event.CLICK, true);
//        options.setEnabled(Options.Event.DOUBLE_CLICK, true);
        options.setFlag(Options.Flag.IGNORE_CLICK_EVENT_ON_GESTURES, true);
        return options;
    }

    private GestureDetector buildMatrixTransformationDetector() {
        Options options = this.options.clone();
        options.set(Options.Constant.MATRIX_MAX_POINTERS_COUNT, 2);
        options.setEnabled(Options.Event.MATRIX_TRANSFORMATION, true);
        EnumSet<Options.Event> allowed = EnumSet.allOf(Options.Event.class);
        allowed.remove(Options.Event.TRANSLATE);
        SelectiveGestureListenerDelegate listenerDelegate = new SelectiveGestureListenerDelegate(this, allowed);
        return new MultipleGestureDetector(listenerDelegate, options);
    }

    public void resetViewPort() {
        adapter.reset();
    }

    @Override
    public void invalidate() {
        pathGestureDetector.invalidate();
        Collection<GestureDetector> values = delegate.getConditionMap().values();
        for (GestureDetector detector : values) {
            if (detector != null) detector.invalidate();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return delegate.onTouch(v, event);
    }

    @Override
    public void onMatrix(GestureEventState state, Matrix currentTransformation) {
        adapter.onMatrix(state, currentTransformation);
    }

    @Override
    public void onDoubleClick(PointF startPoint) {
        resetViewPort();
    }

    @Override
    public void onClick(PointF startPoint) {
        if (imageFragmentSelector != null) {
            imageFragmentSelector.onClickedOnImage(startPoint.x, startPoint.y);
        }
    }

    @Override
    public void onTranslate(GestureEventState state, PointF startPoint, float x, float y, float dx, float dy, double distance) {
        pathGestureDetector.onTranslate(state, startPoint, x, y, dx, dy, distance);
    }

}
