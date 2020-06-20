package com.artemis.player.api;

import android.view.Surface;

/**
 * 播放View接口
 * @author xrealm
 * @date 2020-05-22
 */
public interface IArtemisVideoView {

    Surface getRenderSurface();

    void addSurfaceListener(IVideoSurfaceListener listener);

    void removeSurfaceListener(IVideoSurfaceListener listener);

    interface IVideoSurfaceListener {

        void onSurfaceCreated(Object surface);

        void onSurfaceChanged(Object surface, int width, int height);

        void onSurfaceDestroy(Object surface);
    }
}
