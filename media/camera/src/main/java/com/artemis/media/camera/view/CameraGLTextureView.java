package com.artemis.media.camera.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;

import com.artemis.media.camera.render.ISurfaceRenderer;
import com.artemis.media.filter.view.GLTextureView;

/**
 * Created by xrealm on 2020/6/28.
 */
public class CameraGLTextureView extends GLTextureView implements ICameraView, ISurfaceRenderer {

    private ISurfaceCallback mSurfaceCallback;

    private SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mSurfaceCallback != null) {
                mSurfaceCallback.onSurfaceAvailable(surface, width, height);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (mSurfaceCallback != null) {
                mSurfaceCallback.onSurfaceChanged(surface, width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return mSurfaceCallback == null || mSurfaceCallback.onSurfaceDestroyed(surface);
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            if (mSurfaceCallback != null) {
                mSurfaceCallback.onSurfaceChanged(surface, getWidth(), getHeight());
            }
        }
    };

    public CameraGLTextureView(Context context) {
        super(context);
        initView();
    }

    public CameraGLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    public void setSurfaceCallback(ISurfaceCallback callback) {
        mSurfaceCallback = callback;
    }

    @Override
    public void requestRender(Runnable runnable1, Runnable runnable2) {

    }
}
