package com.artemis.media.filter.base;

import android.opengl.GLES30;

import com.artemis.media.filter.util.ShaderUtil;

public abstract class GLProgram implements IGLProgram {

    public static final String ATTRIBUTE_POSITION = "position";
    public static final String ATTRIBUTE_TEXCOORD = "inputTextureCoordinate";
    public static final String VARYING_TEXCOORD = "textureCoordinate";
    protected static final String UNIFORM_TEXTUREBASE = "inputImageTexture";

    protected int programHandle;
    protected int textureHandle;
    protected int positionHandle;
    protected int texCoordHandle;
    private int[] textureHandles;

    private final int numTextures;


    protected boolean initialized = false;
    protected boolean subSizeChanged = false;

    private int width;
    private int height;

    public GLProgram() {
        this.numTextures = 1;
    }

    public GLProgram(int mumTextures) {
        this.numTextures = mumTextures;
    }

    @Override
    public void setRenderSize(int width, int height) {

        this.width = width;
        this.height = height;
    }

    @Override
    public void drawFrame() {
        if (!initialized) {
            initWithGLContext();
            initialized = true;
        }
        drawSub();
    }

    @Override
    public void destroy() {

    }

    protected void bindShaderAttributes() {

    }

    protected void initShaderHandles() {

    }

    protected void passShaderValues() {

    }

    protected void disableDrawArray() {

    }

    protected void drawSub() {
        GLES30.glUseProgram(programHandle);
        passShaderValues();
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 4);
        disableDrawArray();
    }

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

    protected String getFragmentShader() {
        return "#version 300 es\n"
                + "precision mediump float;\n"
                + "uniform sampler2D " + UNIFORM_TEXTUREBASE + 0 + ";\n"
                + "in vec2 " + VARYING_TEXCOORD + ";\n"
                + "out vec4 fragColor;\n"

                + "void main(){\n"
                + "   fragColor = texture(" + UNIFORM_TEXTUREBASE + 0 + "," + VARYING_TEXCOORD + ");\n"
                + "}\n";
    }

    private void initWithGLContext() {
        programHandle = ShaderUtil.buildProgram(getVertexShader(), getFragmentShader());
        bindShaderAttributes();
        initShaderHandles();
    }

}
