package com.artemis.media.filter.base;

/**
 * Created by xrealm on 2020/7/19.
 */
public interface IGLProgram {

    void setRenderSize(int width, int height);

    void drawFrame();

    void destroy();

}
