package com.master.artemis.player.core;

import com.master.artemis.player.api.IArtemisPlayer;

/**
 * 播放器回调
 * @author xrealm
 * @date 2020-05-22
 */
class ArtemisMediaPlayerListener implements IArtemisPlayer.OnPreparedListener,
        IArtemisPlayer.OnCompletionListener,
        IArtemisPlayer.OnErrorListener {

    private IArtemisPlayer.OnPreparedListener preparedListener;
    private IArtemisPlayer.OnCompletionListener completionListener;
    private IArtemisPlayer.OnErrorListener errorListener;

    public void setOnPreparedListener(IArtemisPlayer.OnPreparedListener listener) {
        preparedListener = listener;
    }

    public void setOnCompletionListener(IArtemisPlayer.OnCompletionListener listener) {
        completionListener = listener;
    }

    public void setOnErrorListener(IArtemisPlayer.OnErrorListener listener) {
        errorListener = listener;
    }

    @Override
    public void onPrepared(IArtemisPlayer mp) {
        if (preparedListener != null) {
            preparedListener.onPrepared(mp);
        }
    }

    @Override
    public void onCompletion(IArtemisPlayer mp) {
        if (completionListener != null) {
            completionListener.onCompletion(mp);
        }
    }

    @Override
    public void onError(IArtemisPlayer mp, int what) {
        if (errorListener != null) {
            errorListener.onError(mp, what);
        }
    }
}
