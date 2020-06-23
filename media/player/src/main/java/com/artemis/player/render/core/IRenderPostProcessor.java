package com.artemis.player.render.core;

import android.view.Surface;

import com.artemis.player.view.IRenderSurface;

/**
 * Created by xrealm on 2020/06/20.
 */
public interface IRenderPostProcessor {

    void setRenderTarget(IRenderSurface renderTarget);

    Surface getRenderSurface();

    void releaseRender();
}
