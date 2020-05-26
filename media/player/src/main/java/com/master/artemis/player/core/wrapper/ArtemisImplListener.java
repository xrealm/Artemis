package com.master.artemis.player.core.wrapper;

import com.master.artemis.player.core.IArtemisPlayerListener;

/**
 * 播放器接口回调
 * @author xrealm
 * @date 2020-05-22
 */
class ArtemisImplListener implements IArtemisPlayerListener.OnPreparedListener,
        IArtemisPlayerListener.OnCompletionListener,
        IArtemisPlayerListener.OnInfoListener,
        IArtemisPlayerListener.OnErrorListener{

    private IArtemisPlayerListener.OnPreparedListener onPreparedListener;
    private IArtemisPlayerListener.OnCompletionListener onCompletionListener;
    private IArtemisPlayerListener.OnInfoListener onInfoListener;
    private IArtemisPlayerListener.OnErrorListener onErrorListener;

    public void setOnPreparedListener(IArtemisPlayerListener.OnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public void setOnCompletionListener(IArtemisPlayerListener.OnCompletionListener onCompletionListener) {
        this.onCompletionListener = onCompletionListener;
    }

    public void setOnInfoListener(IArtemisPlayerListener.OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
    }

    public void setOnErrorListener(IArtemisPlayerListener.OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    @Override
    public void onPrepared() {
        if (onPreparedListener != null) {
            onPreparedListener.onPrepared();
        }
    }

    @Override
    public void onCompletion() {
        if (onCompletionListener != null) {
            onCompletionListener.onCompletion();
        }
    }

    @Override
    public void onInfo(int what, long arg1, long arg2, Object extraObject) {
        if (onInfoListener != null) {
            onInfoListener.onInfo(what, arg1, arg2, extraObject);
        }
    }

    @Override
    public void onError(int errorType, int errorCode, long arg1, long arg2) {
        if (onErrorListener != null) {
            onErrorListener.onError(errorType, errorCode, arg1, arg2);
        }
    }
}
