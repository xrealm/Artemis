package com.artemis.media.camera.render;

import com.artemis.media.filter.api.IGLView;

/**
 * Created by xrealm on 2020/6/28.
 */
public interface ISurfaceRenderer extends IGLView {

    void requestRender(Runnable runnable1, Runnable runnable2);

}
