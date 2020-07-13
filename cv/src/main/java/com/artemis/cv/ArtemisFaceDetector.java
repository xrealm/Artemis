package com.artemis.cv;

import android.content.Context;

import com.artemis.cv.facep.FacePPWrapper;


public class ArtemisFaceDetector {


    private static FacePPWrapper sFaceDetect = new FacePPWrapper();

    private boolean isFaceDetectEnable;

    private ArtemisFaceDetector() {

    }

    public static void init(final Context context) {

       new Thread(new Runnable() {
           @Override
           public void run() {
               sFaceDetect.auth(context, null);
           }
       }).start();

    }

    public static ArtemisFaceDetector getInstance() {
        return FaceDetectorHolder.sInstance;
    }

    private static class FaceDetectorHolder {
        private static ArtemisFaceDetector sInstance = new ArtemisFaceDetector();
    }

    public void setFaceDetectEnable(boolean enable) {
        isFaceDetectEnable = enable;
    }

    public void release() {
        sFaceDetect.release();
    }

    public void processFrame() {
        if (!isFaceDetectEnable) {
            return;
        }
        sFaceDetect.processFrame();
    }
}
