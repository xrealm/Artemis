package com.artemis.media.camera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by xrealm on 2020/6/27.
 */
class CameraSurfaceView extends SurfaceView implements ICameraView {

    private ISurfaceCallback mSurfaceCallback;

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {

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

    public CameraSurfaceView(Context context) {
        super(context);
        initView();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        getHolder().addCallback(mCallback);
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
