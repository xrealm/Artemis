package com.artemis.cv;

import android.content.Context;

import com.artemis.cv.util.FaceThreadPool;

import java.util.concurrent.ExecutorService;

/**
 * Created by xrealm on 2020/7/17.
 */
public class ArtemisFaceDetectModule {

    public static void setThreadPool(ExecutorService executorService) {
        FaceThreadPool.setThreadPool(executorService);
    }

    public static void init(Context context) {
        ArtemisFaceContext.setAppContext(context);
        ArtemisFaceProcessor.init(context);
    }
}
