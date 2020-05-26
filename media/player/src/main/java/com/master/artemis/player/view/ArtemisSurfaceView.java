package com.master.artemis.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 播放器View
 * @author xrealm
 * @date 2020-05-22
 */
public class ArtemisSurfaceView extends SurfaceView implements IPlayerView {

    private IPlayerViewListener mViewListener;

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mViewListener != null) {
                mViewListener.onSurfaceAvailable(holder, getWidth(), getHeight());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mViewListener != null) {
                mViewListener.onSurfaceChanged(holder, width, height);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mViewListener != null) {
                mViewListener.onSurfaceDestroy(holder);
            }
        }
    };


    public ArtemisSurfaceView(Context context) {
        super(context);
        initView();
    }

    public ArtemisSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ArtemisSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public ArtemisSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
//        getHolder().setFormat();
        getHolder().addCallback(mSurfaceCallback);
    }

    @Override
    public void setPlayerViewListener(IPlayerViewListener listener) {
        mViewListener = listener;
    }
}
