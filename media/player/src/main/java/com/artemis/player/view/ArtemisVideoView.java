package com.artemis.player.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;


import com.artemis.player.api.IArtemisVideoView;
import com.artemis.player.log.PlayerLog;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 播放器View
 * @author xrealm
 * @date 2020-05-22
 */
public class ArtemisVideoView extends FrameLayout implements IArtemisVideoView {

    private static final String TAG = "[ArtemisVideoView]";

    private Context mContext;
    private IPlayerView mVideoView;
    private boolean mViewAvailable;
    private Object mSurface;
    private int mWidth;
    private int mHeight;

    private List<IVideoSurfaceListener> mVideoSurfaceListeners = new CopyOnWriteArrayList<>();

    private IPlayerView.IPlayerViewListener mPlayerViewListener = new IPlayerView.IPlayerViewListener() {
        @Override
        public void onSurfaceAvailable(Object surface, int width, int height) {
            PlayerLog.d(TAG, "onSurfaceAvailable, w: " + width + ", h: " + height
                    + ", vW: " + getWidth() + ", vH: " + getHeight());

            mSurface = surface;
            mViewAvailable = true;

            ArtemisVideoView.this.onSurfaceAvailable(surface);
        }

        @Override
        public boolean onSurfaceDestroy(Object surface) {
            PlayerLog.d(TAG, "onSurfaceDestroy");
            mViewAvailable = false;
            ArtemisVideoView.this.onSurfaceDestroy(surface);

            return Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT;
        }

        @Override
        public void onSurfaceChanged(Object surface, int width, int height) {
            PlayerLog.d(TAG, "onSurfaceChanged, w: " + width + ", h: " + height
                    + ", vW: " + getWidth() + ", vH: " + getHeight());
            mSurface = surface;
            mWidth = width;
            mHeight = height;

            ArtemisVideoView.this.onSurfaceChanged(surface, width, height);
        }
    };

    private void onSurfaceAvailable(Object surface) {
        for (IVideoSurfaceListener listener : mVideoSurfaceListeners) {
            listener.onSurfaceCreated(surface);
        }
    }

    private void onSurfaceDestroy(Object surface) {
        for (IVideoSurfaceListener listener : mVideoSurfaceListeners) {
            listener.onSurfaceDestroy(surface);
        }
    }

    private void onSurfaceChanged(Object surface, int width, int height) {
        for (IVideoSurfaceListener listener : mVideoSurfaceListeners) {
            listener.onSurfaceChanged(surface, width, height);
        }
    }

    public ArtemisVideoView(@NonNull Context context) {
        super(context);
        mContext = context.getApplicationContext();
        initView();
    }

    private void initView() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        this.setLayoutParams(params);

        mVideoView = new ArtemisSurfaceView(mContext);
        mVideoView.setPlayerViewListener(mPlayerViewListener);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView((View) mVideoView, layoutParams);
    }


    @Override
    public Surface getRenderSurface() {
        if (!mViewAvailable || mSurface == null) {
            return null;
        }
        if (mSurface instanceof Surface) {
            return (Surface) mSurface;
        } else if (mSurface instanceof SurfaceHolder) {
            return ((SurfaceHolder) mSurface).getSurface();
        } else if (mSurface instanceof SurfaceTexture) {
            return new Surface((SurfaceTexture) mSurface);
        }

        return null;
    }

    @Override
    public void addSurfaceListener(IVideoSurfaceListener listener) {
        if (listener != null && !mVideoSurfaceListeners.contains(listener)) {
            mVideoSurfaceListeners.add(listener);
        }
    }

    @Override
    public void removeSurfaceListener(IVideoSurfaceListener listener) {
        if (listener != null && mVideoSurfaceListeners.contains(listener)) {
            mVideoSurfaceListeners.remove(listener);
        }
    }
}
