package com.master.artemis;

import android.app.Application;

import com.artemis.cv.ArtemisFaceDetector;

public class ArtemisApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ArtemisFaceDetector.init(this);
    }
}
