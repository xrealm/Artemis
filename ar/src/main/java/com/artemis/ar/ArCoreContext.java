package com.artemis.ar;

import android.content.Context;

/**
 * Created by xrealm on 2020/7/22.
 */
public class ArCoreContext {
    private static Context sAppContext;

    public static void setAppContext(Context appContext) {
        sAppContext = appContext.getApplicationContext();
    }

    public static Context getAppContext() {
        return sAppContext;
    }
}
