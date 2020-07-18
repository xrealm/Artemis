package com.artemis.media.filter;

import android.content.Context;

/**
 * Created by xrealm on 2020/7/12.
 */
public class ArtemisFilterContext {

    private static Context sAppContext;

    public static void setAppContext(Context appContext) {
        sAppContext = appContext;
    }

    public static Context getAppContext() {
        return sAppContext;
    }
}
