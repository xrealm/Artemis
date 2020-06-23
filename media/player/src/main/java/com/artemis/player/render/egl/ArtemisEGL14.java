package com.artemis.player.render.egl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLUtils;
import android.view.Surface;

import com.artemis.player.log.PlayerLog;

import javax.microedition.khronos.egl.EGL;

/**
 * Created by xrealm on 2020/06/20.
 */
public class ArtemisEGL14 {

    private static final String TAG = "[ArtemisEGL14.java]";

    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 100;

    private EGLDisplay mEglDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLSurface mEglSurface = EGL14.EGL_NO_SURFACE;
    private EGLContext mEglContext = EGL14.EGL_NO_CONTEXT;

    private EGLConfig mEglConfig = null;

    public boolean createEgl() {
        return createEgl(EGL14.EGL_NO_CONTEXT);
    }

    public boolean createEgl(EGLContext shareContext) {
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
        int[] versions = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, versions, 0, versions, 1)) {
            throw new RuntimeException("eglInitialize failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configAttribs = new int[]{
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_STENCIL_SIZE, 0,
                EGL14.EGL_NONE
        };
        EGL14.eglChooseConfig(mEglDisplay, configAttribs, 0, configs, 0, 1, configsCount, 0);
        if (configsCount[0] <= 0) {
            throw new RuntimeException("eglChooseConfig failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
        PlayerLog.d(TAG, "eglChooseConfig ");
        mEglConfig = configs[0];
        int[] contextAttribs = new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };
        if ((mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, shareContext, contextAttribs, 0)) == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("eglCreateContext failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
        PlayerLog.d(TAG, "eglChooseConfig ");
        int[] values = new int[1];
        EGL14.eglQueryContext(mEglDisplay, mEglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0);

        int[] surfaceAttribs = new int[] {
                EGL14.EGL_WIDTH, DEFAULT_WIDTH,
                EGL14.EGL_HEIGHT, DEFAULT_HEIGHT,
                EGL14.EGL_NONE
        };
        try {
            mEglSurface = EGL14.eglCreatePbufferSurface(mEglDisplay, mEglConfig, surfaceAttribs, 0);
        } catch (Exception e) {
            PlayerLog.e(TAG, e);
            mEglSurface = EGL14.EGL_NO_SURFACE;
        }
        if (mEglSurface == null || mEglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("eglCreatePbufferSurface failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        return true;
    }

    public boolean createScreenEgl(Object screenSurface) {
        return createScreenEgl(EGL14.EGL_NO_CONTEXT, screenSurface);
    }

    public boolean createScreenEgl(EGLContext shareContext, Object screenSurface) {
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
        int[] versions = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, versions, 0, versions, 1)) {
            throw new RuntimeException("eglInitialize failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configAttribs = new int[]{
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_STENCIL_SIZE, 0,
                EGL14.EGL_NONE
        };
        EGL14.eglChooseConfig(mEglDisplay, configAttribs, 0, configs, 0, 1, configsCount, 0);
        if (configsCount[0] <= 0) {
            throw new RuntimeException("eglChooseConfig failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
        PlayerLog.d(TAG, "eglChooseConfig ");
        mEglConfig = configs[0];
        int[] contextAttribs = new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };
        if ((mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, shareContext, contextAttribs, 0)) == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("eglCreateContext failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
        PlayerLog.d(TAG, "eglChooseConfig ");
        int[] values = new int[1];
        EGL14.eglQueryContext(mEglDisplay, mEglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0);

        int[] surfaceAttribs = new int[] {
                EGL14.EGL_NONE
        };
        try {
            mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, screenSurface, surfaceAttribs, 0);
        } catch (Exception e) {
            mEglSurface = EGL14.EGL_NO_SURFACE;
        }
        if (mEglSurface == null || mEglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("eglCreateWindowSurface failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
        return true;
    }

    public boolean makeCurrent(EGLSurface readSurface) {
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY && readSurface != EGL14.EGL_NO_SURFACE && mEglContext != EGL14.EGL_NO_CONTEXT) {
            if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, readSurface, mEglContext)) {
                throw new RuntimeException("eglMakeCurrent failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            }
            return true;
        }
        return false;
    }

    public boolean makeCurrent(Surface surface) {
        EGLSurface eglSurface;
        if (surface == null) {
            int[] configSpec = {
                    EGL14.EGL_WIDTH, DEFAULT_WIDTH,
                    EGL14.EGL_HEIGHT, DEFAULT_HEIGHT,
                    EGL14.EGL_NONE
            };
            eglSurface = EGL14.eglCreatePbufferSurface(mEglDisplay, mEglConfig, configSpec, 0);
        } else {
            eglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, null, 0);
        }

        if (eglSurface == EGL14.EGL_NO_SURFACE || mEglContext == EGL14.EGL_NO_CONTEXT) {
            return false;
        }
        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, mEglContext);
        EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
        mEglSurface = eglSurface;
        if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            return false;
        }
        return true;
    }

    public void swapBuffer() {
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY && mEglSurface != EGL14.EGL_NO_SURFACE) {
            if (!EGL14.eglSwapBuffers(mEglDisplay, mEglSurface)) {

            }
        }
    }

    public int getWidth() {
        int[] value = new int[1];
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY && mEglSurface != EGL14.EGL_NO_SURFACE && mEglContext != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglQuerySurface(mEglDisplay, mEglSurface, EGL14.EGL_WIDTH, value, 0);
        }
        return value[0];
    }

    public int getHeight() {
        int[] value = new int[1];
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY && mEglSurface != EGL14.EGL_NO_SURFACE && mEglContext != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglQuerySurface(mEglDisplay, mEglSurface, EGL14.EGL_HEIGHT, value, 0);
        }
        return value[0];
    }

    public void releaseEgl() {
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY && mEglContext != EGL14.EGL_NO_CONTEXT) {
            if (mEglSurface != EGL14.EGL_NO_SURFACE) {
                if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                    PlayerLog.e(TAG, "releaseEgl failed.");
                }
                EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
            }
            EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroyContext(mEglDisplay, mEglContext);
            EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
            mEglContext = EGL14.EGL_NO_CONTEXT;
            mEglSurface = EGL14.EGL_NO_SURFACE;
            PlayerLog.e(TAG, "releaseEgl.");
        }
    }
}
