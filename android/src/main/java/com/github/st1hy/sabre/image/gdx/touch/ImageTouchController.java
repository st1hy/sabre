package com.github.st1hy.sabre.image.gdx.touch;

import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import com.github.st1hy.core.ImageGdxCore;
import com.github.st1hy.gesturedetector.GestureDetector;
import com.github.st1hy.gesturedetector.MatrixTransformationDetector;
import com.github.st1hy.gesturedetector.Options;
import com.github.st1hy.sabre.image.gdx.touch.transform.AndroidToOpenGLMatrixAdapter;

public class ImageTouchController implements GestureDetector {
    private final AndroidToOpenGLMatrixAdapter adapter;
    private final ImageGdxCoreTransformWrapper gdxCoreTransformWrapper;
    private final DeadZoneOnTouchListener delegate;

    public ImageTouchController(@NonNull ImageGdxCore core) {
        gdxCoreTransformWrapper = new ImageGdxCoreTransformWrapper(core);
        adapter = new AndroidToOpenGLMatrixAdapter(gdxCoreTransformWrapper);
        Options options = new Options();
        options.set(Options.Constant.MATRIX_MAX_POINTERS_COUNT, 3);
        options.setFlag(Options.Flag.MATRIX_OPEN_GL_COMPATIBILITY, true);
        options.setEnabled(Options.Event.MATRIX_TRANSFORMATION, true);
        delegate = new DeadZoneOnTouchListener(new MatrixTransformationDetector(adapter, options));
    }

    public void reset() {
        adapter.reset();
        gdxCoreTransformWrapper.reset();
    }

    @Override
    public void invalidate() {
        delegate.invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return delegate.onTouch(v, event);
    }
}
