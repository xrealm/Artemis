package com.artemis.media.filter.effect;

import android.opengl.GLES30;

import com.artemis.media.filter.filter.BasicFilter;

/**
 * Created by xrealm on 2020/7/16.
 */
public class VignettedFilter extends BasicFilter {

    public static final String UNIFORM_AMOUNT = "amount";
    public static final String UNIFORM_DARKNESS = "darkness";

    private int amountHandle;
    private int darknessHandle;

    private float amount;
    private float darkness;

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setDarkness(float darkness) {
        this.darkness = darkness;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        amountHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_AMOUNT);
        darknessHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_DARKNESS);
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        GLES30.glUniform1f(amountHandle, amount);
        GLES30.glUniform1f(darknessHandle, darkness);
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n" +
                "precision highp float;\n" +
                " in highp vec2 " + VARYING_TEXCOORD + ";\n" +
                " uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n" +
                " uniform highp float " + UNIFORM_AMOUNT + ";\n" +
                " uniform highp float " + UNIFORM_DARKNESS + ";\n" +
                " out vec4 fragColor;" +
                " void main(){\n" +
                "     highp vec2 uv = " + VARYING_TEXCOORD + ";\n" +
                "     highp vec4 origCol = texture(" + UNIFORM_TEXTURE0 + ", uv).rgba;\n" +
                "     highp vec2 uvOffset = (uv - vec2(0.5)) * vec2(" + UNIFORM_AMOUNT + ");\n" +
                "     fragColor = vec4(mix(origCol.rgb, vec3(1.0 - " + UNIFORM_DARKNESS + "), dot(uvOffset,uvOffset)), origCol.a);\n" +
                " }";
    }
}
