package com.artemis.media.filter.effect.glitch;

import android.opengl.GLES30;

import com.artemis.media.filter.filter.BasicDynamicFilter;

public class ScreenShakeGlitchFilter extends BasicDynamicFilter {

    private static final String UNIFORM_INTENSITY = "intensity";

    private int intensityHandle;
    private float intensity;

    public void setIntensity(float intensity) {
        this.intensity = intensity * 0.25f;
    }

    @Override
    protected void bindShaderAttributes() {
        super.bindShaderAttributes();
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
                + "precision highp float;\n"
                + "uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n"
                + "uniform float " + UNIFORM_TIME + ";\n"
                + "uniform float " + UNIFORM_INTENSITY + ";\n"
                + "in vec2 " + VARYING_TEXCOORD + ";\n"
                + "out vec4 fragColor;\n"

                + "float randomNoise(float x, float y) {\n"
                + "    return fract(sin(dot(vec2(x, y), vec2(127.1, 311.7))) * 43758.5453);\n"
                + "}\n"

                + "void main(){\n"
                + "    vec2 uv = textureCoordinate;\n"
                + "    float shake = (randomNoise(iTime, 1.0) - 0.5) * intensity;\n"
                + "    fragColor = texture(" + UNIFORM_TEXTURE0 + ", fract(vec2(uv.x + shake, uv.y)));\n"
                + "}\n";
    }
}
