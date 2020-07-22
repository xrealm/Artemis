package com.artemis.media.filter.filter;

import android.opengl.GLES30;
import android.os.SystemClock;

import com.artemis.media.filter.input.GLTextureOutputRenderer;

/**
 *
 * Created by xrealm on 2020/7/10.
 */
public class BasicDynamicFilter  extends BasicFilter {

    protected static final String UNIFORM_TIME = "iTime";
    protected static final String UNIFORM_RESOLUTION = "iResolution";

    protected int timeHandle;
    protected float timestamp;
    private float renderTime;
    private long startTime;

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp / 1000f;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        timeHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_TIME);
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        renderTime = SystemClock.elapsedRealtime() / 1000f - startTime;
        GLES30.glUniform1f(timeHandle, timestamp > 0 ? timestamp : renderTime);
    }

    @Override
    public void newTextureReady(int texture, GLTextureOutputRenderer source, boolean newData) {
        if (newData) {
            markAsDirty();
        }
        if (startTime == 0) {
            startTime = SystemClock.elapsedRealtime() / 1000L;
        }
        texture_in = texture;
        setWidth(source.getWidth());
        setHeight(source.getHeight());
        onDrawFrame();
        source.unlockRenderBuffer();
    }
}
