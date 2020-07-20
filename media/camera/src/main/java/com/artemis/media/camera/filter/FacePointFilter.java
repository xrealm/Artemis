package com.artemis.media.camera.filter;

import android.opengl.GLES30;

import com.artemis.cv.FrameInfo;
import com.artemis.cv.SingleFaceInfo;
import com.artemis.media.filter.filter.BasicFilter;
import com.artemis.media.filter.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by xrealm on 2020/7/19.
 */
public class FacePointFilter extends BasicFilter implements IFaceFilter {

    private int pointProgramHandle;
    private int pointPositionHandle;

    private FloatBuffer pointVertexBuffer;
    private FrameInfo frameInfo;

    @Override
    public void setFrameInfo(FrameInfo frameInfo) {
        this.frameInfo = frameInfo;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        if (pointProgramHandle == 0) {
            pointProgramHandle = ShaderUtil.buildProgram(getSubVertexShader(), getSubFragmentShader());
        }
        pointPositionHandle = GLES30.glGetAttribLocation(pointProgramHandle, ATTRIBUTE_POSITION);
    }

    @Override
    public void drawSub() {
        super.drawSub();

        if (frameInfo == null || frameInfo.getFaceCnt() == 0) {
            return;
        }

        GLES30.glUseProgram(pointProgramHandle);
        for (int i = 0; i < frameInfo.faceInfos.length; i++) {
            SingleFaceInfo faceInfo = frameInfo.faceInfos[i];
            float[] landmark106 = faceInfo.landmark106;
            if (landmark106 == null) {
                continue;
            }
            if (pointVertexBuffer == null || pointVertexBuffer.capacity() != landmark106.length) {
                pointVertexBuffer = ByteBuffer.allocateDirect(landmark106.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
            }
            pointVertexBuffer.rewind();
            pointVertexBuffer.put(landmark106);
            pointVertexBuffer.position(0);

            GLES30.glEnableVertexAttribArray(pointPositionHandle);
            GLES30.glVertexAttribPointer(pointPositionHandle, 2, GLES30.GL_FLOAT, false, 0, pointVertexBuffer);
            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, landmark106.length / 2);
            GLES30.glDisableVertexAttribArray(pointPositionHandle);
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
                + "   fragColor = vec4(0.0, 1.0, 0.0, 1.0);\n"
                + "}\n";
    }
}
