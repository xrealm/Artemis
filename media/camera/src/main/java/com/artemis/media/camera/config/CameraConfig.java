package com.artemis.media.camera.config;

import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Size;

/**
 * Created by xrealm on 2020/6/26.
 */
public class CameraConfig {

    public int previewVideoWidth;
    public int previewVideoHeight;
    public int previewMinFps;
    public int previewMaxFps;
    public int previewBufferCount;
    public int previewBufferSize;
    public int previewColorFormat;
    private Size previewSize;
    private int facingId;
    private int videoFps;
    private int orientation;

    private CameraConfig() {
    }

    public static CameraConfig obtain() {
        CameraConfig config = new CameraConfig();
        config.previewSize = new Size(1280, 720);
        config.videoFps = 30;
        config.facingId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        config.previewBufferCount = 5;

        return config;
    }

    public static CameraConfig obtainV2() {
        CameraConfig config = new CameraConfig();
        config.previewSize = new Size(1280, 720);
        config.videoFps = 30;
        config.facingId = CameraCharacteristics.LENS_FACING_FRONT;
        config.previewBufferCount = 5;

        return config;
    }

    public Size getPreviewSize() {
        return previewSize;
    }

    public void setPreviewSize(Size previewSize) {
        this.previewSize = previewSize;
    }

    public int getFacingId() {
        return facingId;
    }

    public void setFacingId(int facingId) {
        this.facingId = facingId;
    }

    public int getVideoFps() {
        return videoFps;
    }

    public void setVideoFps(int videoFps) {
        this.videoFps = videoFps;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}
