package com.github.st1hy.sabre.image.gdx.touch;

import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import com.github.st1hy.core.ImageGdxCore;
import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.MatrixTransformationDetector;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.sabre.image.gdx.touch.transform.AndroidToOpenGLMatrixAdapter;
import com.github.st1hy.view.SelectorOnTouchListener;
import com.github.st1hy.view.SimpleTouchPredicate;
import com.github.st1hy.view.TouchPredicate;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageTouchController implements GestureDetector {
    private final AndroidToOpenGLMatrixAdapter adapter;
    private final ImageGdxCoreTransformWrapper gdxCoreTransformWrapper;
    private final SelectorOnTouchListener<GestureDetector> delegate;

    public ImageTouchController(@NonNull ImageGdxCore core) {
        gdxCoreTransformWrapper = new ImageGdxCoreTransformWrapper(core);
        adapter = new AndroidToOpenGLMatrixAdapter(gdxCoreTransformWrapper);
        delegate = buildSelector();
    }

    @NonNull
    private SelectorOnTouchListener<GestureDetector> buildSelector() {
        Map<TouchPredicate, GestureDetector> conditionMap = new HashMap<>();

        TouchPredicate deadZone = new DeadZoneTouchPredicate();
        conditionMap.put(deadZone, null);

        TouchPredicate allElse = new SimpleTouchPredicate(true);
        conditionMap.put(allElse, buildMatrixDetector());

        List<TouchPredicate> order = Lists.newArrayList(deadZone, allElse);
        return new SelectorOnTouchListener<>(conditionMap, order);
    }

    @NonNull
    private MatrixTransformationDetector buildMatrixDetector() {
        Options options = new Options();
        options.set(Options.Constant.MATRIX_MAX_POINTERS_COUNT, 3);
        options.setFlag(Options.Flag.MATRIX_OPEN_GL_COMPATIBILITY, true);
        options.setEnabled(Options.Event.MATRIX_TRANSFORMATION, true);
        return new MatrixTransformationDetector(adapter, options);
    }

    public void reset() {
        adapter.reset();
        gdxCoreTransformWrapper.reset();
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
}
