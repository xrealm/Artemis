package com.artemis.player.api;

import android.view.Surface;

/**
 * 播放View接口
 * @author xrealm
 * @date 2020-05-22
 */
public interface IArtemisVideoView {

    Surface getRenderSurface();

    void addViewSurfaceListener(IVideoViewSurfaceListener listener);

    void removeViewSurfaceListener(IVideoViewSurfaceListener listener);

    interface IVideoViewSurfaceListener {

        void onSurfaceCreated(Object surface);

        void onSurfaceChanged(Object surface, int width, int height);

        void onSurfaceDestroy(Object surface);
    }
}
