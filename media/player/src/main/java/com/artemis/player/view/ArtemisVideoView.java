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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 播放器View
 * @author xrealm
 * @date 2020-05-22
 */
public class ArtemisVideoView extends FrameLayout implements IArtemisVideoView, IRenderSurface {

    private static final String TAG = "[ArtemisVideoView]";

    private Context mContext;
    private IPlayerView mVideoView;
    private boolean mViewAvailable;
    private Object mSurface;
    private int mWidth;
    private int mHeight;

    private List<IVideoViewSurfaceListener> mVideoSurfaceListeners = new CopyOnWriteArrayList<>();
    private List<IRenderSurfaceListener> mRenderSurfaceListener = new CopyOnWriteArrayList<>();

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
        Iterator<IVideoViewSurfaceListener> viewIterator = mVideoSurfaceListeners.iterator();
        while (viewIterator.hasNext()) {
            IVideoViewSurfaceListener lis = viewIterator.next();
            if (lis != null) {
                lis.onSurfaceCreated(surface);
            }
        }

        Iterator<IRenderSurfaceListener> renderIterator = mRenderSurfaceListener.iterator();
        while (renderIterator.hasNext()) {
            IRenderSurfaceListener lis = renderIterator.next();
            if (lis != null) {
                lis.onSurfaceCreate(surface);
            }
        }
    }

    private void onSurfaceDestroy(Object surface) {
        Iterator<IVideoViewSurfaceListener> viewIterator = mVideoSurfaceListeners.iterator();
        while (viewIterator.hasNext()) {
            IVideoViewSurfaceListener lis = viewIterator.next();
            if (lis != null) {
                lis.onSurfaceDestroy(surface);
            }
        }

        Iterator<IRenderSurfaceListener> renderIterator = mRenderSurfaceListener.iterator();
        while (renderIterator.hasNext()) {
            IRenderSurfaceListener lis = renderIterator.next();
            if (lis != null) {
                lis.onSurfaceDestroy(surface);
            }
        }
    }

    private void onSurfaceChanged(Object surface, int width, int height) {
        Iterator<IVideoViewSurfaceListener> viewIterator = mVideoSurfaceListeners.iterator();
        while (viewIterator.hasNext()) {
            IVideoViewSurfaceListener lis = viewIterator.next();
            if (lis != null) {
                lis.onSurfaceChanged(surface, width, height);
            }
        }

        Iterator<IRenderSurfaceListener> renderIterator = mRenderSurfaceListener.iterator();
        while (renderIterator.hasNext()) {
            IRenderSurfaceListener lis = renderIterator.next();
            if (lis != null) {
                lis.onSurfaceChanged(surface, width, height);
            }
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
    public boolean isSurfaceAvailable() {
        return mViewAvailable;
    }

    @Override
    public void setFixSize(int width, int height) {
        if (mWidth == width || mHeight == height) {
            return;
        }
    }

    @Override
    public void addRenderSurfaceListener(IRenderSurfaceListener listener) {
        if (listener != null && !mRenderSurfaceListener.contains(listener)) {
            mRenderSurfaceListener.add(listener);
        }
    }

    @Override
    public void removeRenderSurfaceListener(IRenderSurfaceListener listener) {
        if (listener != null) {
            mRenderSurfaceListener.remove(listener);
        }
    }

    @Override
    public void addViewSurfaceListener(IVideoViewSurfaceListener listener) {
        if (listener != null && !mVideoSurfaceListeners.contains(listener)) {
            mVideoSurfaceListeners.add(listener);
        }
    }

    @Override
    public void removeViewSurfaceListener(IVideoViewSurfaceListener listener) {
        if (listener != null) {
            mVideoSurfaceListeners.remove(listener);
        }
    }
}
