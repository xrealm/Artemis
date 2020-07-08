package com.artemis.media.filter;

import android.opengl.GLES30;
import android.os.Build;
import android.util.Log;

/**
 * Created by xrealm on 2020/6/27.
 */
public class GLFrameBuffer {
    public boolean isLocked;
    protected int[] frameBuffer;
    protected int[] texture_out;
    protected int[] depthRenderBuffer;
    private Object countLock = new Object();
    private int framebufferReferenceCount;
    private int mWidth;
    private int mHeight;
    private boolean isInited = false;
    static boolean isUsedFloat = false;

    private boolean isFloat = false;

    public GLFrameBuffer(int width, int height) {
        this.frameBuffer = new int[1];
        this.texture_out = new int[1];
        this.depthRenderBuffer = new int[1];
        framebufferReferenceCount = 0;
    }

    public boolean cloudRelease() {
        return framebufferReferenceCount <= 0;
    }

    public void lock() {
        synchronized (countLock) {
            isLocked = true;
            framebufferReferenceCount++;
        }
    }

    public void unlock() {
        synchronized (countLock) {
            framebufferReferenceCount--;
            if (framebufferReferenceCount < 1) {
                isLocked = false;
            }
        }
    }

    public int[] getFrameBuffer() {
        return frameBuffer;
    }

    public int[] getTexture_out() {
        return texture_out;
    }

    public int[] getDepthRenderBuffer() {
        return depthRenderBuffer;
    }

    public void destoryBuffer() {
        if (frameBuffer != null) {
            GLES30.glDeleteFramebuffers(1, frameBuffer, 0);
            frameBuffer = null;
        }
        if (texture_out != null) {
            GLES30.glDeleteTextures(1, texture_out, 0);
            texture_out = null;
        }
        if (depthRenderBuffer != null) {
            GLES30.glDeleteRenderbuffers(1, depthRenderBuffer, 0);
            depthRenderBuffer = null;
        }
    }

    public int getBufferWidth() {
        return mWidth;
    }

    public int getBufferHigh() {
        return mHeight;
    }

    public void setFloat(boolean aFloat) {
        //浮点纹理需要5.0以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String str = GLES30.glGetString(GLES30.GL_EXTENSIONS);
            if (str.contains("GL_OES_texture_half_float")) {
                isFloat = aFloat;
            }
        }
    }

    public void activityFrameBuffer(int width, int height) {
        if (isInited) {
            return;
        }
        int[] maxRenderBufferSize = new int[1];
        GLES30.glGetIntegerv(GLES30.GL_MAX_RENDERBUFFER_SIZE, maxRenderBufferSize, 0);
        if (maxRenderBufferSize[0] <= width || maxRenderBufferSize[0] <= height) {
            throw new RuntimeException("failed to create FrameBuffer. width:" + width + ", height:" + height);
        }
        mWidth = width;
        mHeight = height;
        GLES30.glGenFramebuffers(1, frameBuffer, 0);
        GLES30.glGenRenderbuffers(1, depthRenderBuffer, 0);
        GLES30.glGenTextures(1, texture_out, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture_out[0]);

        if (isFloat) {
            int GL_OES_texture_half_float = 0x8D61;
            int GL_RGBA16F_EXT = 0x881a;
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GL_RGBA16F_EXT, width, height, 0, GLES30.GL_RGBA, GL_OES_texture_half_float, null);

            Log.i("tag", "use half float ");
        } else {
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
        }
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthRenderBuffer[0]);
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, width, height);
        Log.w("FBO", "activityFrameBuffer: w:" + width + ", h:" + height + ", tex:" + texture_out[0] + ", fbo:" + frameBuffer[0] + ", rbo:" + depthRenderBuffer[0]);
        Log.i("FBO", "activityFrameBuffer: " + Thread.currentThread().getName());
        Log.i("FBO", "activityFrameBuffer: " + GLES30.glGetError());
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, depthRenderBuffer[0]);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, texture_out[0], 0);
        isInited = true;
    }

}