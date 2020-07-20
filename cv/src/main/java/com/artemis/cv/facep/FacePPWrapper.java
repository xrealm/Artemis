package com.artemis.cv.facep;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

import com.artemis.cv.DetectParams;
import com.artemis.cv.DetectFrame;
import com.artemis.cv.FrameInfo;
import com.artemis.cv.ILicenseListener;
import com.artemis.cv.R;
import com.artemis.cv.SingleFaceInfo;
import com.megvii.facepp.sdk.Facepp;
import com.megvii.licensemanager.sdk.LicenseManager;

public class FacePPWrapper {
    private static final String TAG = "FacePPWrapper";

    private Facepp facepp;
    private Context context;

    public FacePPWrapper() {
        facepp = new Facepp();
    }

    public static void auth(Context context, final ILicenseListener licenseListener) {

        if (Facepp.getSDKAuthType(ConUtil.getFileContent(context, R.raw.megviifacepp_0_5_2_model)) == 2) {
            return;
        }
        final LicenseManager licenseManager = new LicenseManager(context);
        String uuid = ConUtil.getUUIDString(context);
        long apiName = Facepp.getApiName();

        licenseManager.setAuthTimeBufferMillis(0);
        licenseManager.takeLicenseFromNetwork(FacePPConfig.CN_LICENSE_URL, uuid, FacePPConfig.API_KEY,
                FacePPConfig.API_SECRET, apiName, "1",
                new LicenseManager.TakeLicenseCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "success to register facepp licence!");
                        if (licenseListener != null) {
                            licenseListener.onSucess();
                        }
                    }

                    @Override
                    public void onFailed(int i, byte[] bytes) {
                        String msg = new String(bytes);
                        Log.i(TAG, "failed to register facepp licence! what:" + i
                                + ", reason:" + msg);
                        if (licenseListener != null) {
                            licenseListener.onFailed(i, msg);
                        }

                    }
                });
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void release() {
        if (facepp != null) {
            facepp.release();
            facepp = null;
        }
    }

    public void prepare(int rotation, int imageWidth, int imageHeight) {
        facepp.init(context, ConUtil.getFileContent(context, R.raw.megviifacepp_0_5_2_model), 1);
        Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
        faceppConfig.rotation = rotation;
        faceppConfig.interval = 25;
        faceppConfig.minFaceSize = 200;
        faceppConfig.roi_left = 0;
        faceppConfig.roi_top = 0;
        faceppConfig.roi_right = imageWidth;
        faceppConfig.roi_bottom = imageHeight;
        faceppConfig.one_face_tracking = 1;
        faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_FAST;
        facepp.setFaceppConfig(faceppConfig);
    }

    public void processFrame(DetectFrame detectFrame, DetectParams detectParams, FrameInfo frameInfo) {
        if (facepp == null) {
            return;
        }
        Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
        if (faceppConfig.rotation != detectParams.rotation) {
            faceppConfig.rotation = detectParams.rotation;
            facepp.setFaceppConfig(faceppConfig);
        }
        int width = detectFrame.width;
        int height = detectFrame.height;
        int format = getFaceppFormat(detectFrame.format);
        Log.i(TAG, "processFrame: w:" + width + ", h:" + height + ", format:" + format);

        Facepp.Face[] faces = facepp.detect(detectFrame.data, width, height, Facepp.IMAGEMODE_NV21);
        //计算关键点
        if (faces == null || faces.length == 0) {
            return;
        }
        frameInfo.faceInfos = new SingleFaceInfo[faces.length];
        for (int index = 0; index < faces.length; index++) {
            Facepp.Face faceppFace = faces[index];
            SingleFaceInfo singleFaceInfo = new SingleFaceInfo();

            facepp.getLandmarkRaw(faceppFace, Facepp.FPP_GET_LANDMARK106);
            facepp.get3DPose(faceppFace);

            singleFaceInfo.trackId = faceppFace.trackID;
            singleFaceInfo.confidence = faceppFace.confidence;
            singleFaceInfo.pitch = faceppFace.pitch;
            singleFaceInfo.yaw = faceppFace.yaw;
            singleFaceInfo.roll = faceppFace.roll;

            singleFaceInfo.landmark106 = new float[faceppFace.points.length * 2];
            for (int i = 0; i < faceppFace.points.length; i++) {
                float x = (faceppFace.points[i].x / width) * 2 - 1;
                float y = (faceppFace.points[i].y / height) * 2 - 1;
                if (!detectParams.flipShow) {
                    x = -x;
                }
                float[] point = new float[]{y, x};

                singleFaceInfo.landmark106[2 * i] = -point[0];

                singleFaceInfo.landmark106[2 * i + 1] = point[1];
            }
            frameInfo.faceInfos[index] = singleFaceInfo;
            // TODO: 2020/7/20 rect转换重新计算
            Rect rect = faceppFace.rect;
            float top = 1 - (rect.top * 1.0f / height) * 2;
            float left = (rect.left * 1.0f / width) * 2 - 1;
            float right = (rect.right * 1.0f / width) * 2 - 1;
            float bottom = 1 - (rect.bottom * 1.0f / height) * 2;
            singleFaceInfo.faceRect = new float[]{-top, -bottom, -top, left, -bottom, left, -bottom, right};
        }
    }

    private int getFaceppFormat(int format) {

        switch (format) {
            case DetectFrame.FMT_RGBA:
                return Facepp.IMAGEMODE_RGBA;
            case DetectFrame.FMT_NV21:
            default:
                return Facepp.IMAGEMODE_NV21;

        }
    }
}
