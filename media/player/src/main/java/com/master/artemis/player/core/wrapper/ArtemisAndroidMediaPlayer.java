package com.master.artemis.player.core.wrapper;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.TimedText;
import android.os.Build;
import android.view.Surface;

import java.io.IOException;

/**
 * 系统播放器封装
 * @author xrealm
 * @date 2020-05-22
 */
public class ArtemisAndroidMediaPlayer implements IWrapperPlayer {

    private MediaPlayer mediaPlayer;
    private InternalPlayerListener playerListener;

    private ArtemisImplListener wrapperListener;

    public ArtemisAndroidMediaPlayer() {
        playerListener = new InternalPlayerListener();
        wrapperListener = new ArtemisImplListener();
        initPlayer();
    }

    private void initPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(playerListener);
        mediaPlayer.setOnCompletionListener(playerListener);
        mediaPlayer.setOnErrorListener(playerListener);
        mediaPlayer.setOnInfoListener(playerListener);
        mediaPlayer.setOnBufferingUpdateListener(playerListener);
        mediaPlayer.setOnSeekCompleteListener(playerListener);
        mediaPlayer.setOnVideoSizeChangedListener(playerListener);
        mediaPlayer.setOnTimedTextListener(playerListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
        } else {
            mediaPlayer.setAudioStreamType(3);
        }
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        wrapperListener.setOnPreparedListener(listener);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        wrapperListener.setOnCompletionListener(listener);
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        wrapperListener.setOnInfoListener(listener);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        wrapperListener.setOnErrorListener(listener);
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
    public void prepare() throws IOException {
        mediaPlayer.prepare();
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

    }

    @Override
    public void seekTo(int positionMs) {
        mediaPlayer.seekTo(positionMs);
    }

    @Override
    public void setOutputMute(boolean isOutputMote) {
        mediaPlayer.setVolume(1.f, 1.f);
    }

    @Override
    public void setPlaySpeedRatio(int speedRatio) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return;
        }
        PlaybackParams playbackParams = mediaPlayer.getPlaybackParams();
        playbackParams.setSpeed(speedRatio);
        mediaPlayer.setPlaybackParams(playbackParams);
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

    private class InternalPlayerListener implements MediaPlayer.OnPreparedListener,
            MediaPlayer.OnCompletionListener,
            MediaPlayer.OnErrorListener,
            MediaPlayer.OnInfoListener,
            MediaPlayer.OnBufferingUpdateListener,
            MediaPlayer.OnSeekCompleteListener,
            MediaPlayer.OnVideoSizeChangedListener,
            MediaPlayer.OnTimedTextListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            wrapperListener.onPrepared();
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            wrapperListener.onCompletion();
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            wrapperListener.onError(0, 0, what, extra);
            return true;
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            wrapperListener.onInfo(what, extra, 0, 0);
            return true;
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {

        }

        @Override
        public void onSeekComplete(MediaPlayer mp) {

        }

        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

        }

        @Override
        public void onTimedText(MediaPlayer mp, TimedText text) {

        }
    }
}
