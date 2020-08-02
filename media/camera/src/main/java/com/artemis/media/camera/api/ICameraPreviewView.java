package com.artemis.media.camera.api;

import com.artemis.media.camera.render.ISurfaceRenderer;

/**
 * CameraView接口类
 * Created by xrealm on 2020/6/27.
 */
public interface ICameraPreviewView {


    int VIEW_SURFACE = 1;
    int VIEW_TEXTURE = 2;
    int VIEW_GL_TEXTURE = 3;
    int VIEW_GL_SURFACE = 4;

    void setAspectRatio(int width, int height);

    boolean isAvailable();

    ISurfaceRenderer getSurfaceRenderer();

    void addSurfaceCallback(ISurfaceCallback callback);

    void removeSurfaceCallback(ISurfaceCallback callback);

    interface ISurfaceCallback {

        void onSurfaceAvailable(Object surface);

        void onSurfaceDestroyed(Object surface);

        void onSurfaceChanged(Object surface, int width, int height);
    }
}
