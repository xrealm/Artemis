package com.artemis.media.filter.processing;

import android.opengl.GLES30;

import com.artemis.media.filter.filter.BasicFilter;
import com.artemis.media.filter.filter.TwoPassMultiPixelFilter;

/**
 * Created by xrealm on 2020/7/21.
 */
public class RadialBlurFilter extends TwoPassMultiPixelFilter {

    private static final String UNIFORM_INTENSITY = "intensity";

    private int intensityHandle;

    private float intensity;

    /**
     * @param intensity 0-1
     */
    public void setIntensity(float intensity) {
        this.intensity = intensity * 0.02f;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        intensityHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_INTENSITY);
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        GLES30.glUniform1f(intensityHandle, intensity);
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n"
                + "precision mediump float;\n"
                + "uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n"
                + "uniform float " + UNIFORM_INTENSITY + ";\n"
                + "in vec2 " + VARYING_TEXCOORD + ";\n"
                + "out vec4 fragColor;\n"

                + "const vec2 center = vec2(0.5, 0.5);"

                + "void main(){\n"
                + "    vec2 uv = " + VARYING_TEXCOORD + ";\n"
                + "    vec2 blurOffset = (center - uv) * intensity;\n"
                + "    vec4 accumulateColor = vec4(0.0);\n"
                + "    for (int i = 0; i < 10; i++) {\n"
                + "        accumulateColor += texture(" + UNIFORM_TEXTURE0 + ", uv);\n"
                + "        uv += blurOffset;\n"
                + "    }\n"
                + "\n"
                + "   fragColor = accumulateColor / 10.0;\n"
                + "}\n";
    }

}
