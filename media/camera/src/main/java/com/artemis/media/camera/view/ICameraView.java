package com.artemis.media.camera.view;

/**
 * Created by xrealm on 2020/6/27.
 */
interface ICameraView {

    boolean isAvailable();

    void setSurfaceCallback(ISurfaceCallback callback);

    interface ISurfaceCallback {

        void onSurfaceAvailable(Object surface, int width, int height);

        boolean onSurfaceDestroyed(Object surface);

        void onSurfaceChanged(Object surface, int width, int height);
    }

}
