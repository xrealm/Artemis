package com.artemis.media.camera;

import android.graphics.SurfaceTexture;

/**
 * Camera接口类
 * Created by xrealm on 2020/6/25.
 */
public interface ICamera {

    boolean openCamera(int degrees);

    boolean startPreview(SurfaceTexture surfaceTexture);

    void stopPreview();

    boolean switchCamera(int degrees);

    void pauseCamera();

    void resumeCamera();

    void release();

    boolean isFrontCamera();

    int getCameraRotation();

    boolean isSupportFlashMode();

    void setCameraDataCallback(ICameraDataCallback callback);

    void setOnCameraErrorListener(OnCameraErrorListener lis);

    interface OnCameraErrorListener {

        void onCameraError(ICamera camera, int errorCode);
    }
}
