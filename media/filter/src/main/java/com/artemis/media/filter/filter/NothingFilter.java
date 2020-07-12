package com.artemis.media.filter.filter;

import com.artemis.media.filter.api.GLTextureInputRenderer;

/**
 * Created by mocmao on 2020/7/110.
 */
public class NothingFilter extends BasicFilter {

    @Override
    protected void drawFrame() {
        synchronized (listLock) {
            for (GLTextureInputRenderer target : targets) {
                target.newTextureReady(texture_in, this, true);
            }
        }
    }

}
