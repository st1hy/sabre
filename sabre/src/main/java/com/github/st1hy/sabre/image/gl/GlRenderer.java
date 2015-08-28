package com.github.st1hy.sabre.image.gl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.android.grafika.gles.FullFrameRect;
import com.android.grafika.gles.GlUtil;
import com.android.grafika.gles.Texture2dProgram;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "GlRenderer";
    FullFrameRect fullFrameRect;
    int texture = 0;
    private float[] mModelMatrix = GlUtil.IDENTITY_MATRIX;
    private ByteBuffer bitmapBuffer;
    int bWidth, bHeight;

    public void setTexture(Bitmap bitmap) {
        Log.d(TAG, "Setting texture");
        bWidth = bitmap.getWidth();
        bHeight = bitmap.getHeight();
        bitmapBuffer = ByteBuffer.allocateDirect(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(bitmapBuffer);
        bitmap.recycle();
//        loadBitmap();
//            GLUtils.texImage2D(texture, 0, bitmap, 0);
    }

    private void loadBitmap() {
//        texture = GlUtil.createImageTexture(buffer, width, height, GLES20.GL_RGBA);
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.d(TAG, "on surface created");
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        fullFrameRect = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D));
//        if (bitmap != null) loadBitmap();
//        texture = GeneratedTexture.createTestTexture(GeneratedTexture.Image.FINE);
//        GLES20.glDeleteTextures();
//        GlUtil.createImageTexture()
    }

    public void onDrawFrame(GL10 unused) {
        Log.d(TAG, "Drawing frame");
        // Redraw background color

        if (bitmapBuffer !=null)  {
            if (texture != 0) {
                GLES20.glDeleteTextures(1, new int[] {texture}, 0);
            }
            texture = GlUtil.createImageTexture(bitmapBuffer,bWidth, bHeight, GLES20.GL_RGBA);
            bitmapBuffer = null;
//            GLUtils.texImage2D(texture, 0, bitmap, 0);
//            bitmap.recycle();
//            bitmap = null;
//            texture = GeneratedTexture.createTestTexture(GeneratedTexture.Image.FINE);
        }
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        if (texture!=0) fullFrameRect.drawFrame(texture, mModelMatrix);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.d(TAG, "On surface changed");
        GLES20.glViewport(0, 0, width, height); //Reset The Current Viewport

    }

}
