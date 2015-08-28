package com.github.st1hy.sabre.image.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;

public abstract class GlViewer extends GLSurfaceView {
    protected final GlRenderer renderer = new GlRenderer();

    public GlViewer(Context context) {
        super(context);
        init();
    }

    public GlViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setOnClickListener(new OnClickListener() {
//            boolean isContinues = true;
            @Override
            public void onClick(View v) {
                requestRender();
//                setRenderMode(isContinues ? GLSurfaceView.RENDERMODE_WHEN_DIRTY : GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//                isContinues = !isContinues;
            }
        });
    }
}
