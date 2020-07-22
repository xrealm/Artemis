package com.artemis.ar;

import android.content.Context;

/**
 * Created by xrealm on 2020/7/22.
 */
public class ArtemisArCoreModule {

    public static void init(Context context) {
        ArCoreContext.setAppContext(context);
    }


}
