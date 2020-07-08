package com.artemis.media.camera.util;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.util.Size;

import com.artemis.media.camera.config.CameraConfig;
import com.artemis.media.camera.log.CamLog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by xrealm on 2020/6/27.
 */
public class CameraUtil {

    private static final String TAG = "[CameraUtil.java]";

    public static int getDisplayOrientation(int degrees, int cameraId) {
        int displayOrientation = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return displayOrientation;
    }

    public static Size chooseOptimalPreviewSize(Camera.Parameters parameters, CameraConfig config) {
        Size targetSize = config.getPreviewSize();
        List<Camera.Size> previewsSizes = parameters.getSupportedPreviewSizes();
        Collections.sort(previewsSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                if ((lhs.width * lhs.height) > (rhs.width * rhs.height)) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        int previewVideoWidth = 0;
        int previewVideoHeight = 0;
        for (Camera.Size size : previewsSizes) {
            if (size.width >= targetSize.getWidth() && size.height >= targetSize.getHeight()) {
                previewVideoWidth = size.width;
                previewVideoHeight = size.height;
                break;
            }
        }
        Size size = new Size(previewVideoWidth, previewVideoHeight);
        CamLog.i(TAG, "chooseOptimalSize: Size: " + size);
        config.setPreviewSize(size);

        config.previewVideoWidth = previewVideoWidth;
        config.previewVideoHeight = previewVideoHeight;
        //buffer size
        config.previewBufferSize = config.getPreviewSize().getWidth() *
                config.getPreviewSize().getHeight() * 3 / 2;
        return size;
    }

    public static void chooseOptimalFpsRange(Camera.Parameters parameters, CameraConfig config) {
        List<int[]> fpsRanges = parameters.getSupportedPreviewFpsRange();
        int targetFps = config.getVideoFps() * 1000;
        Collections.sort(fpsRanges, new Comparator<int[]>() {
            @Override
            public int compare(int[] lhs, int[] rhs) {
                int r = Math.abs(lhs[0] - targetFps) + Math.abs(lhs[1] - targetFps);
                int l = Math.abs(rhs[0] - targetFps) + Math.abs(rhs[1] - targetFps);
                return Integer.compare(r, l);
            }
        });

        config.previewMinFps = fpsRanges.get(0)[0];
        config.previewMaxFps = fpsRanges.get(0)[1];
    }

    public static boolean chooseCameraColorFormat(Camera.Parameters parameters, CameraConfig config) {
        List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
        if (!supportedPreviewFormats.contains(config.previewColorFormat = ImageFormat.NV21)) {
            CamLog.e(TAG, "unsupoort preview color format NV21");
            return false;
        }
        return true;
    }

    public static boolean configureCamera(Camera camera, CameraConfig config) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes != null) {
            if (((Build.MODEL.startsWith("GT-I950"))
                    || (Build.MODEL.endsWith("SCH-I959"))
                    || (Build.MODEL.endsWith("MEIZU MX3"))) && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            }
        }
        parameters.setPreviewSize(config.getPreviewSize().getWidth(), config.getPreviewSize().getHeight());
        parameters.setPreviewFpsRange(config.previewMinFps, config.previewMaxFps);
        parameters.setPreviewFrameRate(config.getVideoFps());
        parameters.setPreviewFormat(ImageFormat.NV21);
        try {
            camera.setParameters(parameters);
        } catch (Exception e) {
            CamLog.e(TAG, e.getMessage());
            camera.release();
            return false;
        }
        return true;

    }

    public static Size rescalAspectRatio(Size from, int rotation, Size to) {
        return rescalAspectRatio(from, rotation, to, true);
    }

    public static Size rescalAspectRatio(Size from, int rotation, Size to, boolean isHexAlign) {
        Size from1, to1;
        if (rotation == 90 || rotation == 270) {
            from1 = new Size(from.getHeight(), from.getWidth());
            to1 = to;//new Size(to.getHeight(), to.getWidth());
        } else {
            from1 = from;
            to1 = to;
        }
        if (isHexAlign) {
            Size newSize = getDisplaySize(from1, to1, 0);
            int newWidth = (newSize.getWidth()>>4)<<4;
            int newHeight = (newSize.getHeight()>>4)<<4;
            return new Size(newWidth, newHeight);
        } else {
            return getDisplaySize(from1, to1, 0);
        }
    }

    public static Size getDisplaySize(Size previewSize, Size visualSize, int rotateValue) {
        int inputWidth, inputHeight;
        if (rotateValue == 90 || rotateValue == 270) {
            inputHeight = previewSize.getWidth();
            inputWidth = previewSize.getHeight();
        } else {
            inputWidth = previewSize.getWidth();
            inputHeight = previewSize.getHeight();
        }

        int videoWidth = inputWidth;
        int videoHeight = inputHeight;

        int visualWidth = visualSize.getWidth();
        int visualHeight = visualSize.getHeight();

        float wRatio = videoWidth * 1.0f / visualWidth;
        float hRatio = videoHeight * 1.0f / visualHeight;

        float ratio = wRatio < hRatio ? wRatio : hRatio;

        float newWidth = visualWidth * ratio;
        float newHeight = visualHeight * ratio;
        return new Size((int) newWidth, (int) newHeight);
    }

    public static Size reScaleSize(Size from, Size to, int rotation) {

        Size from1, to1;
        if (rotation == 90 || rotation == 270) {
            from1 = new Size(from.getHeight(), from.getWidth());
            to1 = to;
        } else {
            from1 = from;
            to1 = to;
        }

        Size newSize = getDisplaySize(from1, to1, 0);
        int newWidth = (newSize.getWidth()>>4)<<4;
        int newHeight = (newSize.getHeight()>>4)<<4;
        return new Size(newWidth, newHeight);
    }
}
