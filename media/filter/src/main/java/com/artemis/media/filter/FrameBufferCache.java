package com.artemis.media.filter;

import android.util.Size;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by xrealm on 2020/7/23.
 */
public class FrameBufferCache {

    private static final String TAG = "[FrameBufferCache.java]";

    private Map<Size, Queue<GLFrameBuffer>> availableMap = new HashMap<>();
    private Map<Size, Queue<GLFrameBuffer>> allMap = new HashMap<>();
    private Map<Size, Queue<GLFrameBuffer>> usedMap = new HashMap<>();
    private Map<Size, Integer> countMap = new HashMap<>();

    private static final ThreadLocal<FrameBufferCache> INSTANCE = new ThreadLocal<FrameBufferCache>() {

        @Nullable
        @Override
        protected FrameBufferCache initialValue() {
            return new FrameBufferCache();
        }
    };

    public GLFrameBuffer obtain(int width, int height) {

        return null;
    }

    public boolean put(GLFrameBuffer frameBuffer) {

        return false;
    }

    public void shrink() {

    }

    public void destroy() {

    }

    public void recycle() {

    }
}
