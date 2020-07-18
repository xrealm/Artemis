package com.artemis.cv.facep;

import android.content.Context;
import android.util.Log;

import com.artemis.cv.ILicenseListener;
import com.artemis.cv.R;
import com.megvii.facepp.sdk.Facepp;
import com.megvii.licensemanager.sdk.LicenseManager;

public class FacePPWrapper {
    private static final String TAG = "FacePPWrapper";

    private Facepp facepp;

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

    public void init(Context context) {
        facepp.init(context, ConUtil.getFileContent(context, R.raw.megviifacepp_0_5_2_model), 1);
    }

    public void release() {
        if (facepp != null) {
            facepp.release();
            facepp = null;
        }
    }

    public void processFrame() {

    }


}
