package com.artemis.player.core;

import android.content.Context;

import com.artemis.player.api.IArtemisPlayer;
import com.artemis.player.api.IArtemisVideoView;
import com.artemis.player.core.wrapper.ArtemisExoPlayer;
import com.artemis.player.core.wrapper.IWrapperPlayer;
import com.artemis.player.log.PlayerLog;
import com.artemis.player.render.core.RenderPostProcessor;
import com.artemis.player.render.core.IRenderPostProcessor;
import com.artemis.player.view.IRenderSurface;

import java.io.IOException;

/**
 * 播放器实现
 * @author xrealm
 * @date 2020-05-22
 */
public class ArtemisMediaPlayer implements IArtemisPlayer {

    private static final String TAG = "[ArtemisMediaPlayer]";

    private IWrapperPlayer mediaPlayer;

    private ArtemisMediaPlayerListener playerListener;

    private IRenderPostProcessor renderProcessor;

    public ArtemisMediaPlayer(Context context, final IArtemisVideoView videoView) {
        mediaPlayer = new ArtemisExoPlayer(context);
        playerListener = new ArtemisMediaPlayerListener();

        mediaPlayer.setOnPreparedListener(new IArtemisPlayerListener.OnPreparedListener() {
            @Override
            public void onPrepared() {
                PlayerLog.d(TAG, "onPrepared");
                playerListener.onPrepared(ArtemisMediaPlayer.this);
            }
        });

        mediaPlayer.setOnCompletionListener(new IArtemisPlayerListener.OnCompletionListener() {
            @Override
            public void onCompletion() {
                playerListener.onCompletion(ArtemisMediaPlayer.this);
            }
        });

        mediaPlayer.setOnErrorListener(new IArtemisPlayerListener.OnErrorListener() {
            @Override
            public void onError(int errorType, int errorCode, long arg1, long arg2) {
                playerListener.onError(ArtemisMediaPlayer.this, errorCode);
            }
        });

        videoView.addViewSurfaceListener(new IArtemisVideoView.IVideoViewSurfaceListener() {
            @Override
            public void onSurfaceCreated(Object surface) {
                PlayerLog.d(TAG, "onSurfaceCreated, " + videoView.getRenderSurface());
                if (renderProcessor != null) {
                    mediaPlayer.setSurface(renderProcessor.getRenderSurface());
                    renderProcessor.setRenderTarget((IRenderSurface) videoView);
                } else {
                    mediaPlayer.setSurface(videoView.getRenderSurface());
                }
            }

            @Override
            public void onSurfaceChanged(Object surface, int width, int height) {
                PlayerLog.d(TAG, "onSurfaceCreated, " + surface + ", w: " + width + ", h: " + height);
            }

            @Override
            public void onSurfaceDestroy(Object surface) {
                PlayerLog.d(TAG, "onSurfaceDestroy");
            }
        });

        renderProcessor = new RenderPostProcessor(null);
    }


    @Override
    public void openVideo(String url) {
        PlayerLog.i(TAG, "openVideo: " + url);
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        PlayerLog.i(TAG, "start");
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        PlayerLog.i(TAG, "pause");
        mediaPlayer.pause();
    }

    @Override
    public void stop() {
        PlayerLog.i(TAG, "stop");
        mediaPlayer.stop();
        if (renderProcessor != null) {
            renderProcessor.releaseRender();
        }
    }

    @Override
    public void seekTo(int positionMs) {
        PlayerLog.i(TAG, "seekTo");
        mediaPlayer.seekTo(positionMs);
    }

    @Override
    public void setLoopback(boolean isLoopback) {
        mediaPlayer.setLoopback(isLoopback);
    }

    @Override
    public long getDuration() {
        return mediaPlayer.getDurationMs();
    }

    @Override
    public long getCurrentPosition() {
        return mediaPlayer.getCurrentPositionMs();
    }

    @Override
    public int getVideoWidth() {
        return 0;
    }

    @Override
    public int getVideoHeight() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isPausing() {
        return false;
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        playerListener.setOnPreparedListener(listener);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        playerListener.setOnCompletionListener(listener);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        playerListener.setOnErrorListener(listener);
    }

}
