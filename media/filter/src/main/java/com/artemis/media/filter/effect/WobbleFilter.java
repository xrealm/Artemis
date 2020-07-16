package com.artemis.media.filter.effect;

import android.opengl.GLES30;

import com.artemis.media.filter.filter.BasicDynamicFilter;

/**
 * Created by xrealm on 2020/7/14.
 */
public class WobbleFilter extends BasicDynamicFilter {
    public static final String UNIFORM_STRENGTH = "strength";
    public static final String UNIFORM_SIZE = "size";

    private int sizeHandle;
    private int strengthHandle;

    private float size;
    private float strength;

    public void setSize(float size) {
        this.size = size;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        sizeHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_SIZE);
        strengthHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_STRENGTH);
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        GLES30.glUniform1f(sizeHandle, size);
        GLES30.glUniform1f(strengthHandle, strength);
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n" +
                "precision highp float;\n" +
                " in highp vec2 " + VARYING_TEXCOORD + ";\n" +
                " uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n" +
                " uniform highp float " + UNIFORM_STRENGTH + ";\n" +
                " uniform highp int " + UNIFORM_SIZE + ";\n" +
                " uniform highp float " + UNIFORM_TIME + ";\n" +
                " uniform highp vec2 " + UNIFORM_RESOLUTION + ";\n" +
                " out vec4 fragColor;\n" +
                " void main(){\n" +
                "     highp vec2 uv = " + VARYING_TEXCOORD + ";\n" +
                "     float speed = 1.0;\n" +
                "     float TWO_PI = 6.283185307179586;\n" +
                "     vec2 p = -1.0 + 2.0 * uv;\n" +
                "     float pos = " + UNIFORM_TIME + " * TWO_PI + length(p * float(" + UNIFORM_SIZE + "));\n" +
                "     p = uv+" + UNIFORM_STRENGTH + " * vec2(cos(pos),sin(pos));\n" +
                "     fragColor = texture(" + UNIFORM_TEXTURE0 + ",fract(p));\n" +
                " }";
    }
}
