package com.master.artemis.player.core;

/**
 * 播放器接口回调
 * @author xrealm
 * @date 2020-05-22
 */
public interface IArtemisPlayerListener {

    void setOnPreparedListener(OnPreparedListener listener);

    void setOnCompletionListener(OnCompletionListener listener);

    void setOnInfoListener(OnInfoListener listener);

    void setOnErrorListener(OnErrorListener listener);

    interface OnPreparedListener {

        void onPrepared();
    }

    interface OnCompletionListener {

        void onCompletion();
    }

    interface OnInfoListener {

        void onInfo(int what, long arg1, long arg2, Object extraObject);
    }

    interface OnErrorListener {

        void onError(int errorType, int errorCode, long arg1, long arg2);
    }
}
