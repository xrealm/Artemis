package com.artemis.media.filter.util;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;

public class TextureHelper {

    public static int bitmapToTexture(Bitmap bitmap) {
        int[] tex = new int[1];
        GLES30.glGenTextures(1, tex, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, tex[0]);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        if (bitmap != null) {
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        }
        return tex[0];
    }

    public static void deleteTexture(int texture) {
        int[] textures = new int[]{texture};
        GLES30.glDeleteTextures(1, textures, 0);
    }

    public static void deleteTextures(int[] textures) {
        GLES30.glDeleteTextures(textures.length, textures, 0);
    }
}
