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

//        if (frameInfo == null || frameInfo.getFaceCnt() == 0) {
//            return;
//        }


//        float[] landmark106 = frameInfo.faceInfos[0].landmark106;
//        float[] test = new float[]{landmark106[0],landmark106[1]
//                };

       float[] test = new float[]{-0.5f,0.7f,
        };

        if (pointVertexBuffer == null) {
//            pointVertexBuffer = ByteBuffer.allocateDirect(frameInfo.faceInfos[0].landmark106.length * 4)
            pointVertexBuffer = ByteBuffer.allocateDirect(test.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
        }

        GLES30.glUseProgram(pointProgramHandle);

//        SingleFaceInfo faceInfo = frameInfo.faceInfos[0];
        pointVertexBuffer.position(0);
//            pointVertexBuffer.put(faceInfo.landmark106);
        pointVertexBuffer.put(test);
        GLES30.glEnableVertexAttribArray(pointPositionHandle);
//        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 8, pointVertexBuffer);
        GLES30.glVertexAttribPointer(pointPositionHandle, 2, GLES30.GL_FLOAT, false, 0, pointVertexBuffer);
//            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, faceInfo.landmark106.length / 2);
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, test.length/2);
        GLES30.glDisableVertexAttribArray(pointPositionHandle);

//        for (int i = 0; i < frameInfo.faceInfos.length; i++) {
//            SingleFaceInfo faceInfo = frameInfo.faceInfos[i];
//            if (faceInfo.landmark106 == null) {
//                continue;
//            }
//            pointVertexBuffer.position(0);
////            pointVertexBuffer.put(faceInfo.landmark106);
//            pointVertexBuffer.put(test);
//            GLES30.glEnableVertexAttribArray(pointPositionHandle);
//            GLES30.glVertexAttribPointer(pointPositionHandle, 2, GLES30.GL_FLOAT, false, 8, pointVertexBuffer);
////            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, faceInfo.landmark106.length / 2);
//            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 4);
//            GLES30.glDisableVertexAttribArray(pointPositionHandle);
//        }

    }

    @Override
    protected String getVertexShader() {
        return "#version 300 es\n"
                + "layout(location = 0) in vec4 " + ATTRIBUTE_POSITION + ";\n"
                + "layout(location = 1) in vec2 " + ATTRIBUTE_TEXCOORD + ";\n"
                + "out vec2 " + VARYING_TEXCOORD + ";\n"

                + "void main() {\n"
                + "  " + VARYING_TEXCOORD + " = " + ATTRIBUTE_TEXCOORD + ";\n"
                + "   gl_Position = " + ATTRIBUTE_POSITION + ";\n"
                + "}\n";
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n"
                + "precision mediump float;\n"
                + "uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n"
                + "in vec2 " + VARYING_TEXCOORD + ";\n"
                + "out vec4 fragColor;\n"

                + "void main(){\n"
                + "   fragColor = texture(" + UNIFORM_TEXTURE0 + "," + VARYING_TEXCOORD + ");\n"
                + "}\n";
    }

    private String getSubVertexShader() {
        return "#version 300 es\n"
                + "layout(location = 0) in vec4 " + ATTRIBUTE_POSITION + ";\n"
                + "out vec4 pos;"

                + "void main() {\n"
                + "   gl_Position = " + ATTRIBUTE_POSITION + ";\n"
                + "   gl_PointSize = 100.0;"
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
                + "   fragColor = vec4(pos.y);"
                + "}\n";
    }
}
