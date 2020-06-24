package com.artemis.player.render.core;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.artemis.player.render.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by xrealm on 2020/06/20.
 */
public class TextureRender {

    private boolean initialized;
    private int mProgram;
    private int mTextureHandle;

    private FloatBuffer mVerticesBuffer;
    private ShortBuffer mIndicesBuffer;
    private float[] vertices = new float[]{
            1f, 1f,     1f, 1f,
            1f, -1f,    1f, 0f,
            -1f, -1f,   0f, 0f,
            -1f, 1f,     0f, 1f,
    };
    private final short[] drawOrder = {0, 1, 3, 1, 2, 3};
    private int[] mVbos;
    private int mVao;
    private int mTextureId;

    private int mWidth;
    private int mHeight;

    public TextureRender() {
        initialized = false;

        mVerticesBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVerticesBuffer.put(vertices).position(0);

        mIndicesBuffer = ByteBuffer.allocateDirect(drawOrder.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        mIndicesBuffer.put(drawOrder);
        mIndicesBuffer.position(0);

    }

    public void onSizeChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void reInitialize() {
        initialized = false;
    }

    public int getTextureId() {
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);

        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        mTextureId = textures[0];
        return textures[0];
    }

    public void onDrawFrame() {
        if (!initialized) {
            initGLContext();
            initialized = true;
        }
        drawFrame();
    }

    public void destroy() {
        if (mVao != 0) {
            int[] vaos = new int[]{mVao};
            GLES30.glDeleteVertexArrays(1, vaos, 0);
        }
        if (mVbos != null && mVbos.length > 0) {
            GLES30.glDeleteBuffers(mVbos.length, mVbos, 0);
        }
    }

    private void drawFrame() {
        GLES30.glViewport(0, 0, mWidth, mHeight);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES30.glUseProgram(mProgram);
        passShaderValues();
        GLES30.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES30.GL_UNSIGNED_SHORT, 0);
        disableDrawArray();
    }

    private void passShaderValues() {
        GLES30.glBindVertexArray(mVao);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        GLES30.glUniform1i(mTextureHandle, 0);
    }

    protected void disableDrawArray() {
        GLES30.glBindVertexArray(GLES30.GL_NONE);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    private void initShaderHandles() {
        mTextureHandle = GLES30.glGetUniformLocation(mProgram, "iTexture");
    }

    private void initGLContext() {
        if (mProgram == 0) {
            mProgram = ShaderUtil.buildProgram(getVertexShader(), getFragmentShader());
            initShaderHandles();
            initVBO();
        }
    }

    private void initVBO() {
        int[] vbos = new int[2];
        GLES30.glGenBuffers(2, vbos, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.length * 4, mVerticesBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, vbos[1]);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, drawOrder.length * 2, mIndicesBuffer, GLES30.GL_STATIC_DRAW);
        mVbos = vbos;

        int[] vaos = new int[1];
        GLES30.glGenVertexArrays(1, vaos, 0);
        GLES30.glBindVertexArray(vaos[0]);
        mVao = vaos[0];

        GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbos[0]);
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 4 * 4, 0);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 4 * 4, 2 * 4);
        GLES30.glEnableVertexAttribArray(1);

        GLES30.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVbos[1]);
    }

    private String getVertexShader() {
        return  "#version 300 es\n"
                + "layout(location = 0) in vec2 iPosition;\n"
                + "layout(location = 1) in vec2 iTextureCoord;\n"
                + "out vec2 vTextureCoord;\n"
                + "\n"
                + "void main() {\n"
                + "    gl_Position = vec4(iPosition,0.0,1.0);\n"
                + "    vTextureCoord = iTextureCoord;\n"
                + "}\n";

    }

    private String getFragmentShader() {
        return "#version 300 es\n"
                + "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "in vec2 vTextureCoord;\n"
                + "out vec4 fragColor;\n"
                + "uniform samplerExternalOES iTexture;\n"
                + "\n"
                + "void main() {\n"
                + "    vec4 color = texture(iTexture, vec2(vTextureCoord.x, 1.0 - vTextureCoord.y));\n"
                + "    float grey = color.r * 0.299 + color.g * 0.587 + color.b * 0.114;\n"
                + "    fragColor = vec4(vec3(grey), 1.0);\n"
                + "}\n";
    }
}
