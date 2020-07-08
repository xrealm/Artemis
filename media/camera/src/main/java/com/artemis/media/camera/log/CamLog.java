package com.artemis.media.camera.log;

import android.util.Log;

/**
 * Created by xrealm on 2020/6/28.
 */
public class CamLog {

    private static final String TAG_PREFIX = "CamLog_";

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

    public static void e(String tag, Throwable t) {
        Log.e(tag, Log.getStackTraceString(t));
    }
}
