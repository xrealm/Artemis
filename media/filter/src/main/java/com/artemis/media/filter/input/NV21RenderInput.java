package com.artemis.media.filter.input;

import android.opengl.GLES30;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.artemis.media.filter.input.GLTextureOutputRenderer;

/**
 * Created by xrealm on 2020/6/27.
 */
public class NV21RenderInput extends GLTextureOutputRenderer {

    private static final String TAG = "NV21RenderInput";
    public static final int FLIP_VERTICAL = 0;
    public static final int FLIP_HORIZONTAL = 1;
    public static final int FLIP_BOTH = 2;
    public static final int FLIP_NONE = 3;
    private static final String UNIFORM_CAM_MATRIX = "u_Matrix";


    public ByteBuffer mYByteBuffer = null;
    public ByteBuffer mUVByteBufer = null;

    private Object rawDataObj = new Object();
    protected boolean mIsFrameUsed = true;
    // y, u v
    private int[] textures;

    /**
     * Creates a CameraPreviewInput which captures the camera preview with all the default camera parameters and settings.
     */
    public NV21RenderInput() {
        super();
        useNewViewPort = true;
    }

    public boolean isFrameUsed() {
        return mIsFrameUsed;
    }

    public void updateYUVBuffer(byte[] data, int planerSize) {
        if (data == null) {
            return;
        }
        synchronized (rawDataObj) {
            if (this.mYByteBuffer == null || this.mYByteBuffer.capacity() != planerSize) {
                this.mYByteBuffer = ByteBuffer.allocateDirect(planerSize);
            }
            if (this.mUVByteBufer == null || this.mUVByteBufer.capacity() != planerSize / 2) {
                this.mUVByteBufer = ByteBuffer.allocateDirect(planerSize / 2);
            }

            this.mYByteBuffer.clear();
            this.mUVByteBufer.clear();
            this.mYByteBuffer.position(0);
            this.mUVByteBufer.position(0);
            this.mYByteBuffer.put(data, 0, planerSize);
            this.mUVByteBufer.put(data, planerSize, planerSize / 2);

            this.mYByteBuffer.position(0);
            this.mUVByteBufer.position(0);
            mIsFrameUsed = false;
        }
    }

    public void changeCurRotation(int rotation) {
        curRotation = 0;
        rotateClockwise90Degrees(rotation / 90);
    }

    public void flipPosition(int flipDirection) {
        if (flipDirection == FLIP_NONE) {
            setRenderVertices(new float[]{
                    -1f, -1f,
                    1f, -1f,
                    -1f, 1f,
                    1f, 1f
            });
            textureVertices = new FloatBuffer[4];
            float[] texData0 = new float[]{
                    0.0f, 0.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
            };

            textureVertices[0] = ByteBuffer.allocateDirect(texData0.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            textureVertices[0].put(texData0).position(0);

            float[] texData1 = new float[]{
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,
            };
            textureVertices[1] = ByteBuffer.allocateDirect(texData1.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            textureVertices[1].put(texData1).position(0);

            float[] texData2 = new float[]{
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,
            };
            textureVertices[2] = ByteBuffer.allocateDirect(texData2.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            textureVertices[2].put(texData2).position(0);

            float[] texData3 = new float[]{
                    1.0f, 0.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    0.0f, 1.0f,
            };
            textureVertices[3] = ByteBuffer.allocateDirect(texData3.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            textureVertices[3].put(texData3).position(0);
            return;
        }
        float minX = 0f;
        float maxX = 1f;
        float minY = 0f;
        float maxY = 1f;
        switch (flipDirection) {
            case FLIP_VERTICAL:
                minX = 1f;
                maxX = 0f;
                break;
            case FLIP_HORIZONTAL:
                minY = 1f;
                maxY = 0f;
                break;
            case FLIP_BOTH:
                minX = 1f;
                maxX = 0f;
                minY = 1f;
                maxY = 0f;
                break;
            default:
                break;
        }

        float[] texData0 = new float[]{
                minX, minY,
                maxX, minY,
                minX, maxY,
                maxX, maxY,
        };
        textureVertices[0] = ByteBuffer.allocateDirect(texData0.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[0].put(texData0).position(0);

        float[] texData1 = new float[]{
                minY, maxX,
                minY, minX,
                maxY, maxX,
                maxY, minX,
        };
        textureVertices[1] = ByteBuffer.allocateDirect(texData1.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[1].put(texData1).position(0);

        float[] texData2 = new float[]{
                maxX, maxY,
                minX, maxY,
                maxX, minY,
                minX, minY,
        };
        textureVertices[2] = ByteBuffer.allocateDirect(texData2.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[2].put(texData2).position(0);

        float[] texData3 = new float[]{
                maxY, minX,
                maxY, maxX,
                minY, minX,
                minY, maxX,
        };
        textureVertices[3] = ByteBuffer.allocateDirect(texData3.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[3].put(texData3).position(0);

    }

    @Override
    protected void initWithGLContext() {
        super.initWithGLContext();
        textures = new int[2];
        GLES30.glGenTextures(2, textures, 0);
        for (int i = 0; i < 2; i++) {
            int texture = textures[i];
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + i);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture);
            GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, GLES30.GL_TRUE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        }
        GLES30.glUseProgram(programHandle);
        GLES30.glUniform1i(GLES30.glGetUniformLocation(programHandle, "SamplerY"), 0);
        GLES30.glUniform1i(GLES30.glGetUniformLocation(programHandle, "SamplerUV"), 1);
    }

    /* (non-Javadoc)
     * @see com.artemis.media.filter.input.GLTextureOutputRenderer#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        if (texture_in != 0) {
            int[] tex = new int[1];
            tex[0] = texture_in;
            GLES30.glDeleteTextures(1, tex, 0);
            texture_in = 0;
        }
        if (textures != null) {
            GLES30.glDeleteTextures(2, textures, 0);
        }
    }

    @Override
    public void onDrawFrame() {
        markAsDirty();
        super.onDrawFrame();
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n"
                + "precision mediump float;"
                + "uniform sampler2D SamplerY;"
                + "uniform sampler2D SamplerUV;"
                + "in mediump vec2 coordinate;"
                + "out vec4 fragColor;"
                + "void main()"
                + "{" +
                "vec3 rgb;" +
                "vec3 yuv;" +
                //    BT.709, which is the standard for HDTV.
                "yuv.r = texture(SamplerY, coordinate).r - (16.0/255.0);\n" +
                "yuv.g = texture(SamplerUV, coordinate).a - 0.5;\n" +
                "yuv.b = texture(SamplerUV, coordinate).r - 0.5;\n" +

                "   rgb.r = yuv.r + 1.18 * yuv.b;\n" +
                "   rgb.g = yuv.r - 0.34414*yuv.g - 0.71414*yuv.b;\n" +
                "   rgb.b = yuv.r + 1.772*yuv.g;\n" +
                //We finally set the RGB color of our pixel
                "   fragColor = vec4(rgb, 1.0);\n" +
                "}";
    }

    @Override
    protected String getVertexShader() {
        return "#version 300 es\n"
                + "layout(location = 0) in vec4 position;"
                + "layout(location = 1) in  mediump vec4 inputTextureCoordinate;"
                + "out mediump vec2 coordinate;"
                + ""
                + "void main()"
                + "{"
                + "    gl_Position = position;"
                + "    coordinate = inputTextureCoordinate.xy;"
                + "}";
    }

    @Override
    protected void initShaderHandles() {
        textureHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_TEXTURE0);
        positionHandle = GLES30.glGetAttribLocation(programHandle, ATTRIBUTE_POSITION);
        texCoordHandle = GLES30.glGetAttribLocation(programHandle, ATTRIBUTE_TEXCOORD);
    }

    /**
     * Closes and releases the camera for other applications to use.
     * Should be called when the pause is called in the activity.
     */
    public void onPause() {
    }

    /**
     * Re-initializes the camera and starts the preview again.
     * Should be called when resume is called in the activity.
     */
    public void onResume() {
        reInitialize();
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        renderVertices.position(0);
        int width = getWidth();
        int height = getHeight();
        //caculate uv width height just relate to input width  height  not output width height;
        if (curRotation % 2 == 1) {
            width = getHeight();
            height = getWidth();
        }
        GLES30.glUseProgram(programHandle);
        GLES30.glUniform1i(GLES30.glGetUniformLocation(programHandle, "SamplerY"), 0);
        GLES30.glUniform1i(GLES30.glGetUniformLocation(programHandle, "SamplerUV"), 1);

        GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 0, renderVertices);
        GLES30.glEnableVertexAttribArray(positionHandle);
        textureVertices[curRotation].position(0);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, /*drawPreview && mFrontCamera? mirrorTextureVerticesBuffer : */textureVertices[curRotation]);
        GLES30.glEnableVertexAttribArray(1);

        synchronized (rawDataObj) {
            if (mYByteBuffer == null || mUVByteBufer == null) {
                mIsFrameUsed = true;
                return;
            }
            mYByteBuffer.position(0);
            mUVByteBufer.position(0);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, width, height, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, mYByteBuffer);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[1]);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE_ALPHA, width / 2, height / 2, 0, GLES30.GL_LUMINANCE_ALPHA, GLES30.GL_UNSIGNED_BYTE, mUVByteBufer);
            mIsFrameUsed = true;
        }
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
    }
}
