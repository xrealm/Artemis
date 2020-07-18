package com.artemis.cv;

import android.content.Context;

import com.artemis.cv.facep.FacePPWrapper;
import com.artemis.cv.util.FaceThreadPool;


public class ArtemisFaceProcessor {

    private FacePPWrapper faceProcessor;
    private boolean isFaceDetectEnable = true;

    public void init() {
        release();
        if (faceProcessor == null) {
            faceProcessor = new FacePPWrapper();
            faceProcessor.init(ArtemisFaceContext.getAppContext());
        }
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

    public void processFrame() {
        if (!isFaceDetectEnable || faceProcessor == null) {
            return;
        }
        faceProcessor.processFrame();
    }
}
