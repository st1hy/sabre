package com.github.st1hy.sabre.image.gdx.touch;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import com.badlogic.gdx.Gdx;
import com.github.st1hy.core.ImageGdxCore;
import com.github.st1hy.core.screen.ImageScreen;
import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.GestureEventState;
import com.github.st1hy.gesturedetector.MultipleGestureDetector;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.gesturedetector.SimpleGestureListener;
import com.github.st1hy.sabre.image.gdx.touch.transform.AndroidToOpenGLMatrixAdapter;
import com.github.st1hy.view.SelectorOnTouchListener;
import com.github.st1hy.view.SimpleTouchPredicate;
import com.github.st1hy.view.TouchPredicate;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageTouchController extends SimpleGestureListener implements GestureDetector {
    private final Context context;
    private final ImageGdxCore core;
    private final AndroidToOpenGLMatrixAdapter adapter;
    private final ImageSceneTransformWrapper imageTransformWrapper;
    private final SelectorOnTouchListener<GestureDetector> delegate;

    public ImageTouchController(@NonNull Context context, @NonNull ImageGdxCore core) {
        this.context = context;
        this.core = core;
        imageTransformWrapper = new ImageSceneTransformWrapper();
        adapter = new AndroidToOpenGLMatrixAdapter(imageTransformWrapper);
        delegate = buildSelector();
    }

    public void setImageScene(@Nullable ImageScreen imageScreen) {
        imageTransformWrapper.setScene(imageScreen);
    }

    @NonNull
    private SelectorOnTouchListener<GestureDetector> buildSelector() {
        Map<TouchPredicate, GestureDetector> conditionMap = new HashMap<>();

        TouchPredicate deadZone = new DeadZoneTouchPredicate();
        conditionMap.put(deadZone, null);

        TouchPredicate allElse = new SimpleTouchPredicate(true);
        conditionMap.put(allElse, buildMainDetector());

        List<TouchPredicate> order = Lists.newArrayList(deadZone, allElse);
        return new SelectorOnTouchListener<>(conditionMap, order);
    }

    @NonNull
    private GestureDetector buildMainDetector() {
        Options options = new Options(context.getResources());
        for (Options.Event event: Options.Event.values()) {
            options.setEnabled(event, false);
        }
        options.set(Options.Constant.MATRIX_MAX_POINTERS_COUNT, 3);
        options.setFlag(Options.Flag.MATRIX_OPEN_GL_COMPATIBILITY, true);
        options.setEnabled(Options.Event.MATRIX_TRANSFORMATION, true);
        options.setEnabled(Options.Event.DOUBLE_CLICK, true);
        return new MultipleGestureDetector(this, options);
    }

    public void reset() {
        adapter.reset();
        imageTransformWrapper.reset();
    }

    @Override
    public void invalidate() {
        Collection<GestureDetector> values = delegate.getConditionMap().values();
        for (GestureDetector detector : values) {
            detector.invalidate();
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
        reset();
        Gdx.graphics.requestRendering();
    }
}
