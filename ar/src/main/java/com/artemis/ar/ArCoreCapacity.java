package com.artemis.ar;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.google.ar.core.ArCoreApk;

/**
 * Created by xrealm on 2020/7/22.
 */
public class ArCoreCapacity {

    private IArCoreAvailabilityListener availabilityListener;

    public void checkAvailability(final Activity activity) {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(activity.getApplicationContext());
        if (availability == ArCoreApk.Availability.UNKNOWN_ERROR) {
            requestInstall(activity);
            return;
        }
        if (availability.isTransient()) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkAvailability(activity);
                }
            }, 200);
        }
        if (availabilityListener != null) {
            availabilityListener.onAvailability(availability.isSupported());
        }
    }

    public boolean requestInstall(Activity activity) {
        try {
            ArCoreApk.InstallStatus status = ArCoreApk.getInstance().requestInstall(activity, true);
            switch (status) {
                case INSTALLED:
                    return true;
                case INSTALL_REQUESTED:
                default:
                    return false;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setArCoreAvailabilityListener(IArCoreAvailabilityListener listener) {
        this.availabilityListener = listener;
    }

    public interface IArCoreAvailabilityListener {
        void onAvailability(boolean isSupported);
    }
}
