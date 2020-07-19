package com.master.artemis.util;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;

/**
 * Created by xrealm on 2020/7/19.
 */
public class ScreenOrientationHelper extends OrientationEventListener {
    private int oldOrientation = 0;

    private OrientationChangedListener orientationChangedListener;

    public ScreenOrientationHelper(Context context) {
        this(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public ScreenOrientationHelper(Context context, int rate) {
        super(context, rate);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }
        int newOrientation = ((orientation + 45) / 90 * 90) % 360;
        if (newOrientation != oldOrientation) {
            if (orientationChangedListener != null) {
                orientationChangedListener.orientationChanged(newOrientation);

            }
            oldOrientation = newOrientation;
            Log.d("sensor", "rotation is " + newOrientation);
        }
    }

    public void setOrientationChangedListener(OrientationChangedListener orientationChangedListener) {
        this.orientationChangedListener = orientationChangedListener;
    }

    public interface OrientationChangedListener {
        void orientationChanged(int angle);
    }
}
