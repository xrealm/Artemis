package com.artemis.media.filter.processing;

import com.artemis.media.filter.filter.TwoPassMultiPixelFilter;

/**
 * Created by xrealm on 2020/7/10.
 */
public class BoxBlur2Filter extends TwoPassMultiPixelFilter {

    @Override
    protected String getVertexShader() {
        return "#version 300 es\n"
                + "layout(location = 0) in vec4 " + ATTRIBUTE_POSITION + ";\n"
                + "layout(location = 1) in vec2 " + ATTRIBUTE_TEXCOORD + ";\n"
                + "out vec2 " + VARYING_TEXCOORD + ";\n"
                + "out vec4 textureShift_1;\n"
                + "out vec4 textureShift_2;\n"
                + "out vec4 textureShift_3;\n"
                + "out vec4 textureShift_4;\n"
                + "uniform float " + UNIFORM_TEXELWIDTH + ";\n"
                + "uniform float " + UNIFORM_TEXELHEIGHT + ";\n"

                + "void main() {\n"
                + "  " + VARYING_TEXCOORD + " = " + ATTRIBUTE_TEXCOORD + ";\n"
                + "   gl_Position = " + ATTRIBUTE_POSITION + ";\n"
                + "   vec2 stepOffset = vec2(" + UNIFORM_TEXELWIDTH + ", " + UNIFORM_TEXELHEIGHT + ");\n"
                + "   textureShift_1 = vec4(" + ATTRIBUTE_TEXCOORD + ".st - stepOffset, " + ATTRIBUTE_TEXCOORD + ".st + stepOffset);\n"
                + "   textureShift_2 = vec4(" + ATTRIBUTE_TEXCOORD + ".st - 2.0 * stepOffset, " + ATTRIBUTE_TEXCOORD + ".st + 2.0 * stepOffset);\n"
                + "   textureShift_3 = vec4(" + ATTRIBUTE_TEXCOORD + ".st - 3.0 * stepOffset, " + ATTRIBUTE_TEXCOORD + ".st + 3.0 * stepOffset);\n"
                + "   textureShift_4 = vec4(" + ATTRIBUTE_TEXCOORD + ".st - 4.0 * stepOffset, " + ATTRIBUTE_TEXCOORD + ".st + 4.0 * stepOffset);\n"
                + "}\n";
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n"
                + "precision mediump float;\n"
                + "uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n"
                + "in vec2 " + VARYING_TEXCOORD + ";\n"
                + "in vec4 textureShift_1;\n"
                + "in vec4 textureShift_2;\n"
                + "in vec4 textureShift_3;\n"
                + "in vec4 textureShift_4;\n"
                + "out vec4 fragColor;\n"

                + "void main(){\n"
                + "   vec3 sum = texture(" + UNIFORM_TEXTURE0 + "," + VARYING_TEXCOORD + ").rgb;\n"
                + "   sum += texture(" + UNIFORM_TEXTURE0 + ", textureShift_1.xy).rgb;\n"
                + "   sum += texture(" + UNIFORM_TEXTURE0 + ", textureShift_1.zw).rgb;\n"
                + "   sum += texture(" + UNIFORM_TEXTURE0 + ", textureShift_2.xy).rgb;\n"
                + "   sum += texture(" + UNIFORM_TEXTURE0 + ", textureShift_2.zw).rgb;\n"
                + "   sum += texture(" + UNIFORM_TEXTURE0 + ", textureShift_3.xy).rgb;\n"
                + "   sum += texture(" + UNIFORM_TEXTURE0 + ", textureShift_3.zw).rgb;\n"
                + "   sum += texture(" + UNIFORM_TEXTURE0 + ", textureShift_4.xy).rgb;\n"
                + "   sum += texture(" + UNIFORM_TEXTURE0 + ", textureShift_4.zw).rgb;\n"
                + "\n"
                + "   fragColor = vec4(sum * 0.1111, 1.0);\n"
                + "}\n";
    }
}
