package com.master.artemis;

import android.app.Application;

import com.artemis.cv.ArtemisFaceDetectModule;
import com.artemis.media.camera.ArtemisCameraModule;
import com.master.artemis.util.ArtemisThreadPool;

public class ArtemisApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ArtemisCameraModule.init(this);
        ArtemisFaceDetectModule.setThreadPool(ArtemisThreadPool.getThreadPool());
        ArtemisFaceDetectModule.init(this);
    }
}
