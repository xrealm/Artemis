package com.artemis.media.filter.effect;

import android.opengl.GLES30;

import com.artemis.media.filter.filter.BasicDynamicFilter;
import com.artemis.media.filter.filter.BasicFilter;

/**
 * Created by xrealm on 2020/7/16.
 */
public class SolarizedFilter extends BasicFilter {

    private static final String UNIFORM_BRIGHTNESS = "brightness";
    private static final String UNIFORM_POWER = "power";
    private static final String UNIFORM_COLORIZE = "colorize";

    private int brightnessHandle;
    private int powerHandle;
    private int colorizeHandle;

    private float brightness;
    private float power;
    private float colorize;

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public void setColorize(float colorize) {
        this.colorize = colorize;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        brightnessHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_BRIGHTNESS);
        powerHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_POWER);
        colorizeHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_COLORIZE);
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        GLES30.glUniform1f(brightnessHandle, brightness);
        GLES30.glUniform1f(powerHandle, power);
        GLES30.glUniform1f(colorizeHandle, colorize);
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n" +
                "precision highp float;\n" +
                " in highp vec2 " + VARYING_TEXCOORD + ";\n" +
                " uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n" +
                " uniform float " + UNIFORM_BRIGHTNESS + ";\n" +
                " uniform float " + UNIFORM_POWER + ";\n" +
                " uniform float " + UNIFORM_COLORIZE + ";\n" +
                " out vec4 fragColor;" +
                " highp vec3 rgb2hsv(highp vec3 c) {\n" +
                "     highp vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);\n" +
                "     highp vec4 p = c.g < c.b ? vec4(c.bg, K.wz) : vec4(c.gb, K.xy);\n" +
                "     highp vec4 q = c.r < p.x ? vec4(p.xyw, c.r) : vec4(c.r, p.yzx);\n" +
                "     float d = q.x - min(q.w, q.y);\n" +
                "     float e = 1.0e-10;\n" +
                "     return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);\n" +
                " }\n" +
                " highp vec3 hsv2rgb(vec3 c) {\n" +
                "     highp vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);\n" +
                "     highp vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n" +
                "     return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);\n" +
                " }\n" +
                " void main() {\n" +
                "     highp vec2 uv = " + VARYING_TEXCOORD + ";\n" +
                "     highp vec3 origCol = texture(" + UNIFORM_TEXTURE0 + ", uv).rgb;\n" +
                "     highp vec3 hslColor = rgb2hsv(origCol);\n" +
                "     highp vec3 outColor = hslColor;\n" +
                "     outColor.b = pow(outColor.b, " + UNIFORM_POWER + ");\n" +
                "     outColor.b = (outColor.b < " + UNIFORM_BRIGHTNESS + ") ? (1.0 - outColor.b / " + UNIFORM_BRIGHTNESS + ") : (outColor.b - " + UNIFORM_BRIGHTNESS + ") / " + UNIFORM_BRIGHTNESS + ";\n" +
                "     outColor.g = outColor.g * hslColor.b * " + UNIFORM_COLORIZE + ";\n" +
                "     outColor = hsv2rgb(outColor);\n" +
                "     fragColor = vec4(outColor, 1.0);\n" +
                " }";
    }
}
