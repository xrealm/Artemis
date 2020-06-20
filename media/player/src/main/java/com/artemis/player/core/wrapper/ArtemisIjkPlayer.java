package com.artemis.player.core.wrapper;

import android.content.Context;
import android.media.AudioManager;
import android.view.Surface;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * IjkPlayer封装
 * @author xrealm
 * @date 2020-05-22
 */
public class ArtemisIjkPlayer implements IWrapperPlayer {

    private IjkMediaPlayer mediaPlayer;
    private InternalPlayerListener playerListener;

    private OnPreparedListener onPreparedListener;
    private OnCompletionListener onCompletionListener;
    private OnInfoListener onInfoListener;
    private OnErrorListener onErrorListener;

    public ArtemisIjkPlayer(Context context) {
        playerListener = new InternalPlayerListener();
        mediaPlayer = createMediaPlayer();

        mediaPlayer.setOnPreparedListener(playerListener);
        mediaPlayer.setOnVideoSizeChangedListener(playerListener);
        mediaPlayer.setOnCompletionListener(playerListener);
        mediaPlayer.setOnInfoListener(playerListener);
        mediaPlayer.setOnErrorListener(playerListener);
        mediaPlayer.setOnBufferingUpdateListener(playerListener);
        mediaPlayer.setOnSeekCompleteListener(playerListener);
        mediaPlayer.setOnTimedTextListener(playerListener);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setScreenOnWhilePlaying(true);
    }

    private IjkMediaPlayer createMediaPlayer() {
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);

        return ijkMediaPlayer;
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        onPreparedListener = listener;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        onCompletionListener = listener;
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        onInfoListener = listener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        onErrorListener = listener;
    }

    @Override
    public void setSurface(Surface surface) {
        mediaPlayer.setSurface(surface);
    }

    @Override
    public void setDataSource(String path) throws IOException {
        mediaPlayer.setDataSource(path);
    }

    @Override
    public void prepare() {
        mediaPlayer.prepareAsync();
    }

    @Override
    public void prepareAsync() {
        mediaPlayer.prepareAsync();
    }

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void stop() {
        mediaPlayer.stop();
    }

    @Override
    public void reset() {
        mediaPlayer.reset();
    }

    @Override
    public void release() {
        mediaPlayer.release();
    }

    @Override
    public void seekTo(int positionMs) {
        mediaPlayer.seekTo(positionMs);
    }

    @Override
    public void setOutputMute(boolean isOutputMote) {
        mediaPlayer.setVolume(1.0f, 1.0f);
    }

    @Override
    public void setPlaySpeedRatio(int speedRatio) {
        mediaPlayer.setSpeed(speedRatio);
    }

    @Override
    public void setLoopback(boolean isLoopback) {
        mediaPlayer.setLooping(isLoopback);
    }

    @Override
    public long getDurationMs() {
        return mediaPlayer.getDuration();
    }

    @Override
    public long getCurrentPositionMs() {
        return mediaPlayer.getCurrentPosition();
    }

    private class InternalPlayerListener implements IMediaPlayer.OnPreparedListener,
            IMediaPlayer.OnVideoSizeChangedListener,
            IMediaPlayer.OnCompletionListener,
            IMediaPlayer.OnInfoListener,
            IMediaPlayer.OnBufferingUpdateListener,
            IMediaPlayer.OnSeekCompleteListener,
            IMediaPlayer.OnTimedTextListener,
            IMediaPlayer.OnErrorListener {

        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {

            if (onPreparedListener != null) {
                onPreparedListener.onPrepared();
            }
        }

        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

        }

        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            if (onCompletionListener != null) {
                onCompletionListener.onCompletion();
            }
        }

        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            if (onInfoListener != null) {
                onInfoListener.onInfo(i, i1, 0, null);
            }
            return true;
        }

        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

        }

        @Override
        public void onSeekComplete(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {

        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            if (onErrorListener != null) {
                onErrorListener.onError(0, 0, i, i1);
            }
            return true;
        }
    }
}
