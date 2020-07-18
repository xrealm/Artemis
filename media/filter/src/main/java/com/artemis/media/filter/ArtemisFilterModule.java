package com.artemis.media.filter;

import android.content.Context;

/**
 * Created by xrealm on 2020/7/12.
 */
public class ArtemisFilterModule {

    public static void init(Context context) {
        ArtemisFilterContext.setAppContext(context);
    }
}
