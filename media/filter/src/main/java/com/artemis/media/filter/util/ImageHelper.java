package com.artemis.media.filter.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.artemis.media.filter.ArtemisFilterContext;

/**
 * Created by xrealm on 2020/7/12.
 */
public class ImageHelper {

    private static BitmapFactory.Options sOptions;

    static {
        sOptions = new BitmapFactory.Options();
        sOptions.inPremultiplied = false;
    }

    public static Bitmap decodeBitmap(int resId) {
        return BitmapFactory.decodeResource(ArtemisFilterContext.getAppContext().getResources(), resId, sOptions);
    }
}
