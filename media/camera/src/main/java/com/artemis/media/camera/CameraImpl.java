package com.artemis.media.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.artemis.media.camera.config.CameraConfig;
import com.artemis.media.camera.log.CamLog;
import com.artemis.media.camera.util.CameraUtil;

import java.io.IOException;


/**
 * Created by xrealm on 2020/6/27.
 */
public class CameraImpl implements ICamera {

    private static final String TAG = "[CameraImpl.java]";

    private int mCameraCnt;
    private CameraConfig mCamConfig;
    private int mCameraId;
    private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private Camera mCamera;
    private int mCamRotation;
    private boolean isFrontCamera;
    private SurfaceTexture mCameraTexture;

    // TODO: 2020/6/27 加锁很辣鸡，应该单线程操作
    private byte[] mCamSync = new byte[1];

    private ICameraDataCallback mCameraDataCallback;

    public CameraImpl(CameraConfig cameraConfig) {
        mCamConfig = cameraConfig;
        mCameraCnt = Camera.getNumberOfCameras();
    }

    @Override
    public boolean openCamera(int degrees) {
        if ((mCameraCnt - 1) >= mCamConfig.getFacingId()) {
            mCameraId = mCamConfig.getFacingId();
        }
        synchronized (mCamSync) {
            try {
                mCamera = Camera.open(mCameraId);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            isFrontCamera = isFrontCameraInternal();
            setCameraOrientation(degrees);
        }

        Camera.Parameters parameters = mCamera.getParameters();
        CameraUtil.chooseOptimalPreviewSize(parameters, mCamConfig);
        CameraUtil.chooseOptimalFpsRange(parameters, mCamConfig);
        if (!CameraUtil.chooseCameraColorFormat(parameters, mCamConfig)) {
            CamLog.e(TAG, "chooseColorFormat failed.");
            return false;
        }
        if (!CameraUtil.configureCamera(mCamera, mCamConfig)) {
            CamLog.e(TAG, "configureCamera failed.");
            return false;
        }

        for (int i = 0; i < mCamConfig.previewBufferCount; i++) {
            mCamera.addCallbackBuffer(new byte[mCamConfig.previewBufferSize]);
        }

        return true;
    }



    private void setCameraOrientation(int degrees) {
        int displayOrientation = CameraUtil.getDisplayOrientation(degrees, mCameraId);
        int rotation = 0;
        switch (displayOrientation) {
            case 90:
                rotation = 90;
                break;
            case 180:
                rotation = 180;
                break;
            case 270:
                rotation = 270;
                break;
            default:
                rotation = 0;
                break;
        }
        mCamera.setDisplayOrientation(displayOrientation);
        mCamRotation = rotation;
    }

    @Override
    public boolean startPreview(SurfaceTexture surfaceTexture) {
        mCameraTexture = surfaceTexture;
        if (mCamera == null) {
            return false;
        }
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                synchronized (mCamSync) {
                    if (mCameraDataCallback != null) {
                        mCameraDataCallback.onData(data);
                    }
                    if (mCamera != null) {
                        mCamera.addCallbackBuffer(data);
                    }
                }
            }
        });
        synchronized (mCamSync) {
            try {
                mCamera.setPreviewTexture(mCameraTexture);
            } catch (IOException e) {
                e.printStackTrace();
                CamLog.e(TAG, e.getMessage());
                try {
                    mCamera.setPreviewTexture(null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                mCamera.setPreviewCallbackWithBuffer(null);
                mCamera.release();
                mCamera = null;
                return false;
            }
            mCamera.startPreview();
        }
        return true;
    }

    @Override
    public void stopPreview() {
        synchronized (mCamSync) {
            if (mCamera == null) {
                return;
            }
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public boolean switchCamera(int degrees) {
        return false;
    }

    @Override
    public void pauseCamera() {
        synchronized (mCamSync) {
            if (mCamera == null) {
                return;
            }
            mCamera.stopPreview();
        }
    }

    @Override
    public void resumeCamera() {
        if (mCamera == null) {
            return;
        }
        if (mCameraDataCallback != null) {
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (mCameraDataCallback != null) {
                        mCameraDataCallback.onData(data);
                    }
                    if (mCamera != null) {
                        mCamera.addCallbackBuffer(data);
                    }
                }
            });
        }
        mCamera.startPreview();
    }

    @Override
    public void release() {
        synchronized (mCamSync) {
            if (mCamera == null) {
               return;
            }
            mCamera.setPreviewCallback(null);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public boolean isFrontCamera() {
        return isFrontCamera;
    }

    @Override
    public int getCameraRotation() {
        return mCamRotation;
    }

    @Override
    public boolean isSupportFlashMode() {
        return false;
    }

    @Override
    public void setCameraDataCallback(ICameraDataCallback callback) {
        mCameraDataCallback = callback;
    }

    @Override
    public void setOnCameraErrorListener(OnCameraErrorListener lis) {

    }

    private boolean isFrontCameraInternal() {
        try {
            Camera.getCameraInfo(mCameraId, mCameraInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT || mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }
}
