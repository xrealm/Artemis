package com.master.artemis.player.core.wrapper;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

/**
 * ExoPlayer封装
 * @author xrealm
 * @date 2020-05-22
 */
public class ArtemisExoPlayer implements IWrapperPlayer {

    private Context mContext;
    private SimpleExoPlayer mMediaPlayer;
    private MediaSource mMediaSource;
    private Surface mSurface;
    private InternalPlayerListener playerListener;
    private ArtemisImplListener wrapperListener;

    public ArtemisExoPlayer(Context context) {
        mContext = context;
        playerListener = new InternalPlayerListener();
        wrapperListener = new ArtemisImplListener();
        mMediaPlayer = new SimpleExoPlayer.Builder(context).build();

        mMediaPlayer.addListener(playerListener);

    }

    @Override
    public void setSurface(Surface surface) {
        if (surface != null && surface.isValid()) {
            mSurface = surface;
            mMediaPlayer.setVideoSurface(surface);
        }
    }

    @Override
    public void setDataSource(String path) throws IOException {
        Uri uri = Uri.parse(path);
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(
                        mContext, Util.getUserAgent(mContext, "Artemis"));
        mMediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    @Override
    public void prepare() throws IOException {

    }

    @Override
    public void prepareAsync() {
        mMediaPlayer.prepare(mMediaSource);
        mMediaPlayer.setPlayWhenReady(false);
    }

    @Override
    public void start() {
        mMediaPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        mMediaPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stop() {
        mMediaPlayer.stop();
    }

    @Override
    public void reset() {
        mMediaPlayer.stop(true);
    }

    @Override
    public void release() {
        mMediaPlayer.release();
    }

    @Override
    public void seekTo(int positionMs) {

    }

    @Override
    public void setOutputMute(boolean isOutputMote) {

    }

    @Override
    public void setPlaySpeedRatio(int speedRatio) {

    }

    @Override
    public void setLoopback(boolean isLoopback) {

    }

    @Override
    public long getDurationMs() {
        return 0;
    }

    @Override
    public long getCurrentPositionMs() {
        return 0;
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        wrapperListener.setOnPreparedListener(listener);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {

    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {

    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {

    }

    private class InternalPlayerListener implements Player.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_READY:
                    wrapperListener.onPrepared();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPlaybackSuppressionReasonChanged(int playbackSuppressionReason) {

        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {

        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    }
}
