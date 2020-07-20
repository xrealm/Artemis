package com.artemis.media.camera.filter;

import android.opengl.GLES30;

import com.artemis.cv.FrameInfo;
import com.artemis.media.filter.filter.BasicFilter;
import com.artemis.media.filter.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 人脸关键点调试滤镜
 * Created by xrealm on 2020/7/20.
 */
public class FacePointVaoFilter extends BasicFilter implements IFaceFilter {

    private int pointProgram;
    private int pointVbo;
    private int pointVao;

    private FloatBuffer pointVertexBuffer;
    private FrameInfo frameInfo;

    private int pointCount = 106;

    public FacePointVaoFilter() {
        pointVertexBuffer = ByteBuffer.allocateDirect(pointCount * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }

    @Override
    public void setFrameInfo(FrameInfo frameInfo) {
        this.frameInfo = frameInfo;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        if (pointProgram == 0) {
            pointProgram = ShaderUtil.buildProgram(getSubVertexShader(), getSubFragmentShader());
            initPointVBO();
        }
    }


    private void initPointVBO() {
        int[] vbos = new int[1];
        GLES30.glGenBuffers(1, vbos, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, pointCount * 4, pointVertexBuffer, GLES30.GL_STREAM_DRAW);

        pointVbo = vbos[0];

        int[] vaos = new int[1];
        GLES30.glGenVertexArrays(1, vaos, 0);
        GLES30.glBindVertexArray(vaos[0]);
        pointVao = vaos[0];

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, pointVbo);

        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 8, 0);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindVertexArray(GLES30.GL_NONE);

    }

    @Override
    public void drawSub() {

        super.drawSub();

        if (frameInfo == null || !frameInfo.hasFace()) {
            return;
        }

        GLES30.glUseProgram(pointProgram);
        GLES30.glBindVertexArray(pointVao);
        for (int i = 0; i < frameInfo.faceInfos.length; i++) {
            float[] landmark106 = frameInfo.faceInfos[0].landmark106;
            if (pointVertexBuffer == null || pointVertexBuffer.capacity() != landmark106.length) {
                pointVertexBuffer = ByteBuffer.allocateDirect(landmark106.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            }

            pointVertexBuffer.rewind();
            pointVertexBuffer.put(landmark106);
            pointVertexBuffer.position(0);

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, pointVbo);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, pointVertexBuffer.capacity() * 4, pointVertexBuffer, GLES30.GL_STREAM_DRAW);
            GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, pointVertexBuffer.capacity() * 4, pointVertexBuffer);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, landmark106.length / 2);
        }
        GLES30.glBindVertexArray(GLES30.GL_NONE);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (pointVao != 0) {
            int[] vaos = new int[]{pointVao};
            GLES30.glDeleteVertexArrays(1, vaos, 0);
            pointVao = 0;
        }
        if (pointVbo != 0) {
            int[] vbos = new int[]{pointVbo};
            GLES30.glDeleteBuffers(1, vbos, 0);
            pointVbo = 0;
        }
    }

    private String getSubVertexShader() {
        return "#version 300 es\n"
                + "layout(location = 0) in vec4 " + ATTRIBUTE_POSITION + ";\n"
                + "out vec4 pos;"

                + "void main() {\n"
                + "   gl_Position = " + ATTRIBUTE_POSITION + ";\n"
                + "   gl_PointSize = 5.0;"
                + "   pos = gl_Position;"
                + "}\n";
    }

    private String getSubFragmentShader() {
        return "#version 300 es\n"
                + "precision mediump float;\n"
                + "in vec4 pos;\n"
                + "out vec4 fragColor;\n"

                + "void main(){\n"
                + "   fragColor = vec4(0.0, 1.0, 1.0, 1.0);\n"
                + "}\n";
    }
}
