package com.artemis.player.view;

import android.view.Surface;

/**
 * Created by xrealm on 2020/06/20.
 */
public interface IRenderSurface {

    Surface getRenderSurface();

    boolean isSurfaceAvailable();

    void setFixSize(int width, int height);

    void addRenderSurfaceListener(IRenderSurfaceListener listener);

    void removeRenderSurfaceListener(IRenderSurfaceListener listener);

    interface IRenderSurfaceListener {

        void onSurfaceCreate(Object surface);

        void onSurfaceDestroy(Object surface);

        void onSurfaceChanged(Object surface, int width, int height);
    }
}
