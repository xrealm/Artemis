package com.artemis.media.camera.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.artemis.media.camera.render.ISurfaceRenderer;

/**
 * Created by xrealm on 2020/8/2.
 */
public class CameraGLSurfaceView extends GLSurfaceView implements ICameraView, ISurfaceRenderer {

    private ISurfaceCallback mSurfaceCallback;

    private SurfaceHolder.Callback mInternalCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mSurfaceCallback != null) {
                mSurfaceCallback.onSurfaceAvailable(holder, getWidth(), getHeight());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mSurfaceCallback != null) {
                mSurfaceCallback.onSurfaceChanged(holder, width, height);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mSurfaceCallback != null) {
                mSurfaceCallback.onSurfaceDestroyed(holder);
            }
        }
    };

    public CameraGLSurfaceView(Context context) {
        super(context);
        initView();
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        getHolder().addCallback(mInternalCallback);
    }

    @Override
    public void requestRender(Runnable runnable1, Runnable runnable2) {

    }

    @Override
    public boolean isAvailable() {
        return getHolder().getSurface() != null;
    }

    @Override
    public void setSurfaceCallback(ISurfaceCallback callback) {
        mSurfaceCallback = callback;
    }
}
