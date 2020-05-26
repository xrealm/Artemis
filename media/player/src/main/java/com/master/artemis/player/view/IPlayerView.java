package com.master.artemis.player.view;

/**
 * 播放器View接口
 * @author xrealm
 * @date 2020-05-22
 */
public interface IPlayerView {

    void setPlayerViewListener(IPlayerViewListener listener);


    interface IPlayerViewListener {

        void onSurfaceAvailable(Object surface, int width, int height);

        boolean onSurfaceDestroy(Object surface);

        void onSurfaceChanged(Object surface, int width, int height);
    }
}
