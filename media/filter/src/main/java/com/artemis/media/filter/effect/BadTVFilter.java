package com.artemis.media.filter.effect;

import android.opengl.GLES30;

import com.artemis.media.filter.filter.BasicDynamicFilter;

/**
 * Created by xrealm on 2020/7/13.
 */
public class BadTVFilter extends BasicDynamicFilter {

    public static final String UNIFORM_THICK_DISTORT = "thickDistort";
    public static final String UNIFORM_FINE_DISTORT = "fineDistort";
    public static final String UNIFORM_ROLL_SPEED = "rollSpeed";

    private int thickDistortHandle;
    private int fineDistortHandle;
    private int rollSpeedHandle;

    private float thickDistortValue = 1.3f;
    private float fineDistortValue = 1.3f;
    private float rollDistortValue = 1f;

    public void setThickDistortValue(float value) {
        this.thickDistortValue = value;
    }

    public void setFineDistortValue(float value) {
        this.fineDistortValue = value;
    }

    public void setRollDistortValue(float value) {
        this.rollDistortValue = value;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        thickDistortHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_THICK_DISTORT);
        fineDistortHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_FINE_DISTORT);
        rollSpeedHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_ROLL_SPEED);
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        GLES30.glUniform1f(thickDistortHandle, thickDistortValue);
        GLES30.glUniform1f(fineDistortHandle, fineDistortValue);
        GLES30.glUniform1f(rollSpeedHandle, rollDistortValue);
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n" +
                "precision highp float;\n" +
                " in highp vec2 " + VARYING_TEXCOORD + ";\n" +
                " uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n" +
                " uniform highp float " + UNIFORM_TIME + ";\n" +
                " uniform highp vec2 iResolution;\n" +
                " uniform float " + UNIFORM_THICK_DISTORT + ";\n" +
                " uniform float " + UNIFORM_FINE_DISTORT + ";\n" +
                " uniform float " + UNIFORM_ROLL_SPEED + ";\n" +
                " float speed = 0.116;\n" +
                " out vec4 fragColor;\n" +
                " highp vec3 mod289(highp vec3 x) {\n" +
                "     return x - floor(x * (1.0 / 289.0)) * 289.0;\n" +
                " }\n" +
                " highp vec2 mod289(highp vec2 x) {\n" +
                "     return x - floor(x * (1.0 / 289.0)) * 289.0;\n" +
                " }\n" +
                " highp vec3 permute(highp vec3 x) {\n" +
                "     return mod289(((x*34.0)+1.0)*x);\n" +
                " }\n" +
                " float snoise(highp vec2 v) {\n" +
                "     const highp vec4 C = vec4(0.211324865405187, 0.366025403784439, -0.577350269189626, 0.024390243902439);\n" +
                "     highp vec2 i = floor(v + dot(v, C.yy) );\n" +
                "     highp vec2 x0 = v - i + dot(i, C.xx);\n" +
                "     highp vec2 i1; i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);\n" +
                "     highp vec4 x12 = x0.xyxy + C.xxzz;\n" +
                "     x12.xy -= i1;\n" +
                "     i = mod289(i);\n" +
                "     highp vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 )) + i.x + vec3(0.0, i1.x, 1.0 ));\n" +
                "     highp vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);\n" +
                "     m = m*m ; m = m*m ;\n" +
                "     highp vec3 x = 2.0 * fract(p * C.www) - 1.0;\n" +
                "     highp vec3 h = abs(x) - 0.5;\n" +
                "     highp vec3 ox = floor(x + 0.5);\n" +
                "     highp vec3 a0 = x - ox;\n" +
                "     m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );\n" +
                "     highp vec3 g; g.x = a0.x * x0.x + h.x * x0.y;\n" +
                "     g.yz = a0.yz * x12.xz + h.yz * x12.yw;\n" +
                "     return 130.0 * dot(m, g);\n" +
                " }\n" +
                " void main() {\n" +
                "     highp vec2 uv = " + VARYING_TEXCOORD + ";\n" +
                "     highp vec2 p = uv;\n" +
                "     float time = " + UNIFORM_TIME + " * 0.25;\n" +
                "     float ty = time * speed * 17.346;\n" +
                "     float yt = p.y - ty;\n" +
                "     float offset = snoise(vec2(yt*3.0,0.0))*0.2;\n" +
                "     offset = offset*" + UNIFORM_THICK_DISTORT + " * offset*" + UNIFORM_THICK_DISTORT + " * offset;\n" +
                "     offset += snoise(vec2(yt*50.0,0.0))*" + UNIFORM_FINE_DISTORT + "*0.002;\n" +
                "     fragColor = texture(" + UNIFORM_TEXTURE0 + ",fract(vec2(fract(p.x + offset),fract(p.y - time * " + UNIFORM_ROLL_SPEED + "))));\n" +
                " }";
    }
}
