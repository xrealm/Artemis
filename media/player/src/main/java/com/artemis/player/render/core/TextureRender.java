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
    private int mPositionHandle;
    private int mTexCoordHandle;
    private int mTextureHandle;

    private FloatBuffer mVerticesBuffer;
    private FloatBuffer mTextureCoordBuffer;
    private ShortBuffer mIndicesBuffer;
    private final short[] drawOrder = {0, 1, 2, 2, 1, 3};
    private int mTextureId;

    private int mWidth;
    private int mHeight;

    public TextureRender() {
        initialized = false;
        float[] vertices = new float[]{
                -1f, -1f,
                -1f, 1f,
                1f, -1f,
                1f, 1f
        };
        mVerticesBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVerticesBuffer.put(vertices).position(0);

        float[] texData = new float[]{
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
        };

        mTextureCoordBuffer = ByteBuffer.allocateDirect(texData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(texData);
        mTextureCoordBuffer.position(0);

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

    private void passShaderValues() {
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 8, mVerticesBuffer);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 8, mTextureCoordBuffer);
        GLES30.glEnableVertexAttribArray(1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        GLES30.glUniform1i(mTextureHandle, 0);
    }

    protected void disableDrawArray() {
        GLES30.glDisableVertexAttribArray(mPositionHandle);
        GLES30.glDisableVertexAttribArray(mTexCoordHandle);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    private void initShaderHandles() {
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "iPosition");
        mTexCoordHandle = GLES30.glGetAttribLocation(mProgram, "iTextureCoord");
        mTextureHandle = GLES30.glGetUniformLocation(mProgram, "iTexture");
    }

    private void initGLContext() {
        if (mProgram == 0) {
            mProgram = ShaderUtil.buildProgram(getVertexShader(), getFragmentShader());
            initShaderHandles();
        }
    }

    public void onDrawFrame() {
        if (!initialized) {
            initGLContext();
            initialized = true;
        }
        drawFrame();
    }

    private void drawFrame() {
        GLES30.glViewport(0, 0, mWidth, mHeight);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES30.glUseProgram(mProgram);
        passShaderValues();
        GLES30.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES30.GL_UNSIGNED_SHORT, mIndicesBuffer);
        disableDrawArray();
    }

    private String getVertexShader() {
        return  "#version 300 es\n"
                + "layout(location = 0) in vec4 iPosition;\n"
                + "layout(location = 1) in vec2 iTextureCoord;\n"
                + "out vec2 vTextureCoord;\n"
                + "\n"
                + "void main() {\n"
                + "    gl_Position = iPosition;\n"
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
                + "    vec4 color = texture(iTexture, vTextureCoord);\n"
                + "    float grey = color.r * 0.299 + color.g * 0.587 + color.b * 0.114;\n"
                + "    fragColor = vec4(vec3(grey), 1.0);\n"
                + "}\n";
    }
}
