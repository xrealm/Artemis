package com.artemis.media.filter;

/**
 * Created by xrealm on 2020/6/28.
 */
public class ArtemisNativeFilter {

    static {
        System.loadLibrary("native-filter");
    }

    private native int _create();
}
