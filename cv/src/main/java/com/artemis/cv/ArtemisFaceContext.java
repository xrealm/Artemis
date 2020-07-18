package com.artemis.cv;

import android.content.Context;

/**
 * Created by xrealm on 2020/7/17.
 */
public class ArtemisFaceContext {

    private static Context sAppContext;

    public static void setAppContext(Context appContext) {
        sAppContext = appContext;
    }

    public static Context getAppContext() {
        return sAppContext;
    }
}
