package com.artemis.media.camera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.artemis.media.camera.api.ICameraPreviewView;
import com.artemis.media.camera.log.CamLog;
import com.artemis.media.camera.render.ISurfaceRenderer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by xrealm on 2020/6/27.
 */
public class CameraPreviewView extends FrameLayout implements ICameraPreviewView {

    private static final String TAG = "[CameraPreviewView.java]";

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private ICameraView mCameraView;
    private List<ISurfaceCallback> mSurfaceCallbacks = new CopyOnWriteArrayList<>();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VIEW_SURFACE, VIEW_TEXTURE, VIEW_GL_TEXTURE})
    public @interface viewType {
    }

    public CameraPreviewView(@NonNull Context context) {
        super(context);
        initView(VIEW_SURFACE);
    }

    public CameraPreviewView(@NonNull Context context, @viewType int viewType) {
        super(context);
        initView(viewType);
    }

    public CameraPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(VIEW_SURFACE);
    }

    private void initView(@viewType int viewType) {
        if (viewType == VIEW_GL_TEXTURE) {
            mCameraView = new CameraGLTextureView(getContext());
        } else if (viewType == VIEW_TEXTURE) {
            mCameraView = new CameraTextureView(getContext());
        } else {
            mCameraView = new CameraSurfaceView(getContext());
        }
        addView((View) mCameraView);
        mCameraView.setSurfaceCallback(new ICameraView.ISurfaceCallback() {
            @Override
            public void onSurfaceAvailable(Object surface, int width, int height) {
                CamLog.i(TAG, "onSurfaceAvailable: " + surface + ", w:" + width + ", h:" + height);
                handleSurfaceAvailable(surface);
            }

            @Override
            public boolean onSurfaceDestroyed(Object surface) {
                CamLog.i(TAG, "onSurfaceDestroyed: " + surface);
                handleSurfaceDestroy(surface);
                return true;
            }

            @Override
            public void onSurfaceChanged(Object surface, int width, int height) {
                handleSurfaceChanged(surface, width, height);
            }
        });
    }

    private void handleSurfaceAvailable(Object surface) {
        Iterator<ISurfaceCallback> iterator = mSurfaceCallbacks.iterator();
        while (iterator.hasNext()) {
            ISurfaceCallback lis = iterator.next();
            if (lis != null) {
                lis.onSurfaceAvailable(surface);
            }
        }
    }

    private void handleSurfaceDestroy(Object surface) {
        Iterator<ISurfaceCallback> iterator = mSurfaceCallbacks.iterator();
        while (iterator.hasNext()) {
            ISurfaceCallback lis = iterator.next();
            if (lis != null) {
                lis.onSurfaceDestroyed(surface);
            }
        }
    }

    private void handleSurfaceChanged(Object surface, int width, int height) {
        Iterator<ISurfaceCallback> iterator = mSurfaceCallbacks.iterator();
        while (iterator.hasNext()) {
            ISurfaceCallback lis = iterator.next();
            if (lis != null) {
                lis.onSurfaceChanged(surface, width, height);
            }
        }
    }


    @Override
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mRatioWidth == 0 || mRatioHeight == 0) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }

        int windowWidth = right - left;
        int windowHeight = bottom - top;
        int videoWidth = mRatioWidth;
        int videoHeight = mRatioHeight;
        int dx = 0, dy = 0;
        float scale;
        if (videoWidth * windowHeight > windowWidth * videoHeight) {
            scale = (float) windowHeight / (float) videoHeight;
            dx = (int) ((windowWidth - videoWidth * scale) * 0.5f);
        } else {
            scale = (float) windowWidth / (float) videoWidth;
            dy = (int) ((windowHeight - videoHeight * scale) * 0.5f);
        }
        CamLog.i(TAG, "onLayout: left:" + (left + dx));
        CamLog.i(TAG, "onLayout: top:" + (top + dy));

        ((View) mCameraView).layout(left + dx, top + dy, windowWidth - dx, windowHeight - dy);
    }


    @Override
    public boolean isAvailable() {
        return mCameraView.isAvailable();
    }

    @Override
    public ISurfaceRenderer getSurfaceRenderer() {
        return (ISurfaceRenderer) mCameraView;
    }

    @Override
    public void addSurfaceCallback(ISurfaceCallback callback) {
        if (callback != null && !mSurfaceCallbacks.contains(callback)) {
            mSurfaceCallbacks.add(callback);
        }
    }

    @Override
    public void removeSurfaceCallback(ISurfaceCallback callback) {
        if (callback != null) {
            mSurfaceCallbacks.remove(callback);
        }
    }


}
