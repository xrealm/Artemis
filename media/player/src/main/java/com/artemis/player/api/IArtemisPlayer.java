package com.artemis.player.api;

/**
 * 播放器对外接口
 * @author xrealm
 * @date 2020-05-22
 */
public interface IArtemisPlayer {

    void openVideo(String url);

    void start();

    void pause();

    void stop();

    void seekTo(int positionMs);

    void setLoopback(boolean isLoopback);

    long getDuration();

    long getCurrentPosition();

    int getVideoWidth();

    int getVideoHeight();

    boolean isPlaying();

    boolean isPausing();

    void setOnPreparedListener(OnPreparedListener listener);

    void setOnCompletionListener(OnCompletionListener listener);

    void setOnErrorListener(OnErrorListener listener);

    interface OnPreparedListener {
        void onPrepared(IArtemisPlayer mp);
    }

    interface OnCompletionListener {
        void onCompletion(IArtemisPlayer mp);
    }

    interface OnErrorListener {
        void onError(IArtemisPlayer mp, int what);
    }
}
