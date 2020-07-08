package com.artemis.media.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;

import com.artemis.media.camera.config.CameraConfig;
import com.artemis.media.camera.log.CamLog;

/**
 * Created by xrealm on 2020/6/27.
 */
public class CameraAdapter implements ICamera, ICameraDataCallback {

    private static final String TAG = "[CameraAdapter.java]";

    private ICamera mCamera;
    private ICameraDataCallback mCameraDataCallback;
    private SurfaceTexture mCameraTexture;
    private CameraConfig mCamConfig;

    private Context mContext;

    public CameraAdapter(Context context, CameraConfig cameraConfig) {
        this.mContext = context;
        this.mCamConfig = cameraConfig;

//        mCamera = new CameraImpl(cameraConfig);
        mCamera = new Camera2Impl(context, cameraConfig);

    }

    @Override
    public boolean openCamera(int degrees) {
        if (!mCamera.openCamera(degrees)) {
            CamLog.e(TAG, "open camera failed.");
            return false;
        }
        mCamera.setCameraDataCallback(this);

        return true;
    }

    @Override
    public boolean startPreview(SurfaceTexture surfaceTexture) {
        mCameraTexture = surfaceTexture;
        return mCamera.startPreview(surfaceTexture);
    }

    @Override
    public void stopPreview() {
        mCamera.stopPreview();
    }

    @Override
    public boolean switchCamera(int degrees) {
        return mCamera.switchCamera(degrees);
    }

    @Override
    public void pauseCamera() {
        mCamera.pauseCamera();
    }

    @Override
    public void resumeCamera() {
        mCamera.resumeCamera();
    }

    @Override
    public void release() {
        mCamera.release();
    }

    @Override
    public boolean isFrontCamera() {
        return mCamera.isFrontCamera();
    }

    @Override
    public int getCameraRotation() {
        return mCamera.getCameraRotation();
    }

    @Override
    public boolean isSupportFlashMode() {
        return mCamera.isSupportFlashMode();
    }

    @Override
    public void setCameraDataCallback(ICameraDataCallback callback) {
        mCameraDataCallback = callback;
    }

    @Override
    public void setOnCameraErrorListener(OnCameraErrorListener lis) {

    }

    @Override
    public void onData(byte[] data) {
        if (mCameraDataCallback != null) {
            mCameraDataCallback.onData(data);
        }
    }

    public ICamera getCamera() {
        return mCamera;
    }
}
