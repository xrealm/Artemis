package com.artemis.player.log;

import android.util.Log;

/**
 * 播放器日志
 * @author xrealm
 * @date 2020-05-22
 */
public class PlayerLog {

    private static final String TAG_PREFIX = "PlayerLog_";

    public static void v(String tag, String msg) {
        Log.v(TAG_PREFIX + tag, msg);
    }

    public static void d(String tag, String msg) {
        Log.d(TAG_PREFIX + tag, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(TAG_PREFIX + tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(TAG_PREFIX + tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG_PREFIX + tag, msg);
    }
}
