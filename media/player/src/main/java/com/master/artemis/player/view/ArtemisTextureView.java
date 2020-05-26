package com.master.artemis.player.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * 播放器View
 * @author xrealm
 * @date 2020-05-22
 */
public class ArtemisTextureView extends TextureView implements IPlayerView {

    private IPlayerViewListener mViewListener;

    private SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mViewListener != null) {
                mViewListener.onSurfaceAvailable(surface, width, height);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (mViewListener != null) {
                mViewListener.onSurfaceChanged(surface, width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return mViewListener == null || mViewListener.onSurfaceDestroy(surface);
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            if (mViewListener != null) {
                mViewListener.onSurfaceChanged(surface, getWidth(), getHeight());
            }
        }
    };

    public ArtemisTextureView(Context context) {
        super(context);
        initView();
    }

    public ArtemisTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ArtemisTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public ArtemisTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        setOpaque(true);
        setAlpha(1.0f);
        setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    public void setPlayerViewListener(IPlayerViewListener listener) {
        mViewListener = listener;
    }
}
