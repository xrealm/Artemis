package com.artemis.cv;

import android.content.Context;

import com.artemis.cv.facep.FacePPWrapper;
import com.artemis.cv.util.FaceThreadPool;


public class ArtemisFaceProcessor {

    private FacePPWrapper faceProcessor;
    private boolean isFaceDetectEnable = true;

    public void prepare(int rotation, int imageWidth, int imageHeight) {
        release();
        faceProcessor = new FacePPWrapper();
        faceProcessor.setContext(ArtemisFaceContext.getAppContext());
        faceProcessor.prepare(rotation, imageWidth, imageHeight);
    }

    public void setFaceDetectEnable(boolean enable) {
        isFaceDetectEnable = enable;
    }

    public void release() {
        if (faceProcessor != null) {
            faceProcessor.release();
        }
    }

    public static void init(final Context context) {

       FaceThreadPool.execute(new Runnable() {
           @Override
           public void run() {
               FacePPWrapper.auth(context, null);
           }
       });

    }

    public static ArtemisFaceProcessor getInstance() {
        return FaceDetectorHolder.sInstance;
    }

    private static class FaceDetectorHolder {
        private static ArtemisFaceProcessor sInstance = new ArtemisFaceProcessor();
    }

    public void processFrame(DetectFrame detectFrame, DetectParams detectParams, FrameInfo frameInfo) {
        if (!isFaceDetectEnable || faceProcessor == null) {
            return;
        }
        faceProcessor.processFrame(detectFrame, detectParams, frameInfo);
    }
}
