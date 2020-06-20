package com.artemis.player.api;

import android.content.Context;

import com.artemis.player.core.ArtemisMediaPlayer;
import com.artemis.player.view.ArtemisVideoView;

/**
 *
 * @author xrealm
 * @date 2020-05-22
 */
public class MediaPlayerFactory {

    public static IArtemisVideoView createVideoView(Context context) {
        return new ArtemisVideoView(context);
    }



    public static IArtemisPlayer createMediaPlayer(Context context, IArtemisVideoView videoView) {
        return new ArtemisMediaPlayer(context, videoView);
    }
}
