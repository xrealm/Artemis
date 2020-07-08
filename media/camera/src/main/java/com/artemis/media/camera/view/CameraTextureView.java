package com.artemis.media.camera.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by xrealm on 2020/6/27.
 */
class CameraTextureView extends TextureView implements ICameraView {

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

    public CameraTextureView(Context context) {
        super(context);
        initView();
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CameraTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    public void setSurfaceCallback(ISurfaceCallback callback) {
        mSurfaceCallback = callback;
    }
}
