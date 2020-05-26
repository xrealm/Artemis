package com.master.artemis.player.core.wrapper;

import android.view.Surface;

import com.master.artemis.player.core.IArtemisPlayerListener;

import java.io.IOException;

/**
 * 播放器接口
 * @author xrealm
 * @date 2020-05-22
 */
public interface IWrapperPlayer extends IArtemisPlayerListener {

    void setSurface(Surface surface);

    void setDataSource(String path) throws IOException;

    void prepare() throws IOException;

    void prepareAsync();

    void start();

    void pause();

    void stop();

    void reset();

    void release();

    void seekTo(int positionMs);

    void setOutputMute(boolean isOutputMote);

    void setPlaySpeedRatio(int speedRatio);

    void setLoopback(boolean isLoopback);

    long getDurationMs();

    long getCurrentPositionMs();
}
