package com.artemis.media.filter.api;

import java.util.List;

/**
 * Created by xrealm on 2020/7/10.
 */
public interface IGLTextureOutputRenderer {

    void addTarget(GLTextureInputRenderer target);

    void removeTarget(GLTextureInputRenderer target);

    void clearTarget();

    List<GLTextureInputRenderer> getTargets();

    int getTextOutId();
}
