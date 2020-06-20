package com.master.artemis;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.artemis.player.api.IArtemisPlayer;
import com.artemis.player.api.IArtemisVideoView;
import com.artemis.player.api.MediaPlayerFactory;

import java.lang.ref.WeakReference;

public class VideoTestActivity extends AppCompatActivity {

    private IArtemisPlayer mediaPlayer;
    private IArtemisVideoView videoView;

    private ImageView playControllerIv;
    private TextView curTimeTv;
    private TextView totalTimeTv;
    private SeekBar progressSeekBar;

    private EventHandler eventHandler = new EventHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_test);
        initView();

        FrameLayout playerRootView = findViewById(R.id.fl_player_root);
        IArtemisVideoView videoView = MediaPlayerFactory.createVideoView(this);
        playerRootView.addView((View) videoView);
        this.videoView = videoView;

        mediaPlayer = MediaPlayerFactory.createMediaPlayer(getApplicationContext(), this.videoView);
        mediaPlayer.setOnPreparedListener(new IArtemisPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IArtemisPlayer mp) {
                startPlay();
            }
        });

        mediaPlayer.setOnCompletionListener(new IArtemisPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IArtemisPlayer mp) {

            }
        });

        mediaPlayer.setOnErrorListener(new IArtemisPlayer.OnErrorListener() {
            @Override
            public void onError(IArtemisPlayer mp, int what) {
                eventHandler.sendEmptyMessage(EventHandler.MSG_PLAY_ERROR);
            }
        });

        loadVideo();
    }

    private void initView() {
        playControllerIv = findViewById(R.id.iv_player_controller);
        curTimeTv = findViewById(R.id.tv_player_time);
        totalTimeTv = findViewById(R.id.tv_total_time);
        progressSeekBar = findViewById(R.id.sb_indicator);
        curTimeTv.setText("00:00");
    }

    private void loadVideo() {

        String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f30.mp4";
        url = "http://vfx.mtime.cn/Video/2019/03/18/mp4/190318214226685784.mp4";
        mediaPlayer.openVideo(url);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumePlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        pausePlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlay();
    }

    private void startPlay() {
        mediaPlayer.start();
        eventHandler.sendEmptyMessage(EventHandler.MSG_GET_POS);

        long totalDuration = mediaPlayer.getDuration() / 1000;
        totalTimeTv.setText(convertTimeToString(totalDuration));
        progressSeekBar.setMax((int) totalDuration);
    }

    private void pausePlay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }

        eventHandler.removeCallbacksAndMessages(null);
        playControllerIv.setImageDrawable((getResources().getDrawable(R.drawable.ic_play_arrow_24dp)));
    }

    private void resumePlay() {
        if (mediaPlayer.isPausing()) {
            mediaPlayer.start();
        }
        eventHandler.sendEmptyMessageDelayed(EventHandler.MSG_GET_POS, 500);
        playControllerIv.setImageDrawable((getResources().getDrawable(R.drawable.ic_pause_24dp)));
    }

    private void stopPlay() {
        mediaPlayer.stop();
        eventHandler.removeCallbacksAndMessages(null);
        playControllerIv.setImageDrawable((getResources().getDrawable(R.drawable.ic_play_arrow_24dp)));
    }

    static String convertTimeToString(long time) {
        String result = "";
        if (time / 60 > 0) {
            result += time / 60;
            result += ":";
            result += time % 60 < 10 ? "0" + time % 60 : "" + time % 60;
        } else {
            result += "00:";
            result += time < 10 ? "0" + time : "" + time;
        }
        return result;
    }

    private static class EventHandler extends Handler {

        static final int MSG_GET_POS = 1;
        static final int MSG_PLAY_ERROR = 4;

        WeakReference<VideoTestActivity> outerRef;

        public EventHandler(VideoTestActivity outer) {
            outerRef = new WeakReference<>(outer);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            VideoTestActivity outer = outerRef.get();
            if (outer == null) {
                return;
            }
            switch (msg.what) {
                case MSG_GET_POS:
                    long currentPosition = outer.mediaPlayer.getCurrentPosition() / 1000;
                    outer.curTimeTv.setText(convertTimeToString(currentPosition));
                    int progress = (int) currentPosition;
                    outer.progressSeekBar.setProgress(progress);
                    sendEmptyMessageDelayed(MSG_GET_POS, 500);
                    break;
                case MSG_PLAY_ERROR:
                    Toast.makeText(outer, "播放错误", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }
}
