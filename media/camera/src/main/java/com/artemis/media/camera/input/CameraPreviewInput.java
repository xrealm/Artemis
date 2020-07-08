package com.artemis.media.camera.input;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;

import com.artemis.media.camera.CameraAdapter;
import com.artemis.media.camera.ICamera;
import com.artemis.media.camera.ICameraDataCallback;
import com.artemis.media.camera.config.CameraConfig;
import com.artemis.media.camera.log.CamLog;
import com.artemis.media.camera.util.CameraUtil;
import com.artemis.media.camera.util.TextureRotationUtil;
import com.artemis.media.egl.EglCore;
import com.artemis.media.egl.util.GLUtil;
import com.artemis.media.filter.input.NV21RenderInput;
import com.artemis.media.filter.view.GLTextureView;

import java.lang.ref.WeakReference;

/**
 * Created by xrealm on 2020/6/27.
 */
public class CameraPreviewInput extends NV21RenderInput implements ICameraDataCallback {

    private static final String TAG = "CameraNV21PreviewInput";

    private CameraAdapter mCameraAdapter;
    private CameraConfig mCamConfig;
    private GLTextureView mRenderer;

    private Object mCameraSync = new Object();

    int frames = 0;

    CameraEGLThread mEglThread = null;
    private int mTextureID = 0;
    private SurfaceTexture mCameraTexture = null;

    public boolean isFrontCamera() {
        return mCameraAdapter.isFrontCamera();
    }

    public CameraPreviewInput(Context context, CameraConfig cameraConfig) {
        super();
        if (null == cameraConfig) {
            this.mCamConfig = CameraConfig.obtain();
        } else {
            this.mCamConfig = cameraConfig;
        }
        mCameraAdapter = new CameraAdapter(context, cameraConfig);
        useNewViewPort = true;
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n"
                + "precision mediump float;"
                + "uniform sampler2D SamplerY;"
                + "uniform sampler2D SamplerUV;"
                + "in mediump vec2 coordinate;"
                + "out vec4 fragColor;"
                + "void main()"
                + "{" +
                "vec3 rgb;" +
                "vec3 yuv;" +
                //    BT.709, which is the standard for HDTV.
                "yuv.r = texture(SamplerY, coordinate).r - (16.0/255.0);\n" +
                "yuv.g = texture(SamplerUV, coordinate).a - 0.5;\n" +
                "yuv.b = texture(SamplerUV, coordinate).r - 0.5;\n" +

                "   rgb.r = yuv.r + 1.18 * yuv.b;\n" +
                "   rgb.g = yuv.r - 0.34414*yuv.g - 0.71414*yuv.b;\n" +
                "   rgb.b = yuv.r + 1.772*yuv.g;\n" +
                //We finally set the RGB color of our pixel
                "   fragColor = vec4(rgb, 1.0);\n" +
                "}";
    }


    public void setRenderer(GLTextureView mRenderer) {
        this.mRenderer = mRenderer;
    }

    public boolean startPreview(Activity activity) {
        return startPreview(TextureRotationUtil.getRotationAngle(activity));
    }

    public boolean startPreview(int degree) {
        CamLog.d(TAG, "startPreview");

        CamLog.d(TAG, "startPreview degree:" + degree);

        if (!mCameraAdapter.openCamera(degree)) {
            CamLog.e(TAG, "Camera prepare Failed!");
            return false;
        }

        mCameraAdapter.setOnCameraErrorListener(new ICamera.OnCameraErrorListener() {
            @Override
            public void onCameraError(ICamera camera, int errorCode) {
            }
        });


        mCameraAdapter.setCameraDataCallback(this);

        getCameraTexture();
        mCameraTexture.setDefaultBufferSize(mCamConfig.previewVideoWidth, mCamConfig.previewVideoHeight);
        Size vSize = CameraUtil.reScaleSize(new Size(mCamConfig.previewVideoWidth, mCamConfig.previewVideoHeight), new Size(9, 16), mCameraAdapter.getCameraRotation());

        if (mCameraAdapter.isFrontCamera()) {
            changeCurRotation(360 - mCameraAdapter.getCameraRotation());
            flipPosition(NV21RenderInput.FLIP_BOTH);
        } else {
            changeCurRotation(mCameraAdapter.getCameraRotation());
            flipPosition(NV21RenderInput.FLIP_HORIZONTAL);
        }
        setRenderSize(mCamConfig.previewVideoWidth, mCamConfig.previewVideoHeight);
        mCameraAdapter.startPreview(mCameraTexture);

        return true;
    }




    public void stopPreview() {
        if (mCameraAdapter == null) {
            return;
        }
        synchronized (mCameraSync) {
            CamLog.d(TAG, "stopPreview");
            mCameraAdapter.stopPreview();
            mCameraAdapter.release();
            frames = 0;
        }

        releaseCameraTexture();
    }


    public void releaseCamera() {
        synchronized (mCameraSync) {
            mCameraAdapter.release();

            releaseCameraTexture();
        }
    }


    /** (non-Javadoc)
     * @see com.artemis.media.filter.input.GLTextureOutputRenderer#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();

    }

    @Override
    public void onDrawFrame() {
        markAsDirty();
        super.onDrawFrame();
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
    }

    /**
     * Closes and releases the camera for other applications to use.
     * Should be called when the pause is called in the activity.
     */
    @Override
    public void onPause() {
        mCameraAdapter.setCameraDataCallback(null);
    }


    /**
     * Re-initializes the camera and starts the preview again.
     * Should be called when resume is called in the activity.
     */
    @Override
    public void onResume() {
        reInitialize();

        getCameraTexture();
        if (null != mCameraTexture) {
            mCameraAdapter.setCameraDataCallback(this);
        }
    }

    @Override
    public void onData(final byte[] data) {
        if (null == mCameraAdapter.getCamera() || data == null) {
            return;
        }
        processCameraData(data);
    }

    private void processCameraData(byte[] data) {
        if (!isFrameUsed()) {
            return;
        }
        synchronized (mCameraSync) {
            updateYuv(data);
        }
    }


    private void updateYuv(byte[] data) {
        int planerSize = mCamConfig.previewVideoWidth * mCamConfig.previewVideoHeight;
        updateYUVBuffer(data, planerSize);

        try {
            if (null != mRenderer) {
                mRenderer.requestRender();
            }
        } catch (Exception e) {
            CamLog.e(TAG, e);
        }

    }

    public SurfaceTexture getCameraTexture() {
        if (mEglThread != null && mTextureID > 0 && mCameraTexture != null) {
            return mCameraTexture;
        }

        mEglThread = new CameraEGLThread();
        mEglThread.start();
        mEglThread.waitUntilReady();

        return mCameraTexture;
    }

    private void releaseCameraTexture() {
        if (mEglThread != null && mEglThread.getHandler() != null) {
            mEglThread.getHandler().sendShutdown();
            try {
                mEglThread.join();
            } catch (InterruptedException ie) {
            }

            mEglThread = null;
        }
    }


    /**
     * Thread that handles all rendering and camera operations.
     */
    private class CameraEGLThread extends Thread {
        // Used to wait for the thread to start.
        private Object mStartLock = new Object();
        private boolean mReady = false;

        private CameraEGLHandler mHandler = null;
        private EglCore mEglCore;

        /**
         * Constructor.  Pass in the MainHandler, which allows us to send stuff back to the
         * Activity.
         */
        public CameraEGLThread() {
            super("CameraEGLThread");
        }

        /**
         * Returns the render thread's Handler.  This may be called from any thread.
         */
        public CameraEGLHandler getHandler() {
            return mHandler;
        }

        protected SurfaceTexture createTexture() {
            if (mCameraTexture == null) {
                mTextureID = GLUtil.generateOESTexture();
                mCameraTexture = new SurfaceTexture(mTextureID);

                Log.e(TAG, "createTexture: mTextureID:" + mTextureID + ", mCameraTexture:" + mCameraTexture);
            }

            return mCameraTexture;
        }

        protected void releaseTexture() {
            if (mCameraTexture != null) {
                mCameraTexture.release();
                Log.e(TAG, "releaseCameraTexture mCameraTexture: " + mCameraTexture);
                mCameraTexture = null;
            }

            if (mTextureID != 0) {
                int[] textures = new int[]{mTextureID};
                GLES30.glDeleteTextures(1, textures, 0);
                Log.e(TAG, "releaseCameraTexture glDeleteTextures: " + mTextureID);
                mTextureID = 0;
            }
        }

        /**
         * Thread entry point.
         */
        @Override
        public void run() {
            Looper.prepare();

            mHandler = new CameraEGLHandler(this);
            // Prepare EGL and open the camera before we start handling messages.
            mEglCore = new EglCore();
            mEglCore.createDummyEgl();

            Log.e(TAG, "run: Start" );

            mEglCore.makeCurrent();
            createTexture();

            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();    // signal waitUntilReady()
            }

            Looper.loop();

            releaseTexture();

            mEglCore.releaseEgl();
            mHandler = null;

            synchronized (mStartLock) {
                mReady = false;
            }

            Log.e(TAG, "run: Exit" );
        }

        /**
         * Waits until the render thread is ready to receive messages.
         * <p>
         * Call from the UI thread.
         */
        public void waitUntilReady() {
            synchronized (mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait();
                    } catch (InterruptedException ie) { /* not expected */ }
                }
            }
        }

        /**
         * Shuts everything down.
         */
        private void shutdown() {
            Looper.myLooper().quit();
        }
    }

    private static class CameraEGLHandler extends Handler {
        private static final int MSG_SHUTDOWN = 3;

        // This shouldn't need to be a weak ref, since we'll go away when the Looper quits,
        // but no real harm in it.
        private WeakReference<CameraEGLThread> mWeakCameraEGLThread;

        /**
         * Call from CameraEGLThread thread.
         */
        public CameraEGLHandler(CameraEGLThread rt) {
            mWeakCameraEGLThread = new WeakReference<CameraEGLThread>(rt);
        }

        /**
         * Sends the "shutdown" message, which tells the render thread to halt.
         * <p>
         * Call from UI thread.
         */
        public void sendShutdown() {
            sendMessage(obtainMessage(MSG_SHUTDOWN));
        }


        @Override  // runs on RenderThread
        public void handleMessage(Message msg) {
            int what = msg.what;

            CameraEGLThread eglThread = mWeakCameraEGLThread.get();
            if (eglThread == null) {
                return;
            }

            switch (what) {
                case MSG_SHUTDOWN:
                    eglThread.shutdown();
                    break;
                default:
                    break;
            }
        }
    }

}
