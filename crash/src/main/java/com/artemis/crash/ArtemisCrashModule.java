package com.artemis.crash;

import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by xrealm on 2020/7/23.
 */
public class ArtemisCrashModule {

    public static void init(Context context) {
        CrashReport.UserStrategy userStrategy = new CrashReport.UserStrategy(context);
        CrashReport.initCrashReport(context, "d4a297ed6e", true, userStrategy);
    }
}
