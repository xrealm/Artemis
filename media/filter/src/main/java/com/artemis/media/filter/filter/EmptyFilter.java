package com.artemis.media.filter.filter;

/**
 * Created by xrealm on 2020/6/28.
 */
public class EmptyFilter extends BasicFilter {

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n"
                + "precision mediump float;\n"
                + "uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n"
                + "in vec2 " + VARYING_TEXCOORD + ";\n"
                + "out vec4 fragColor;\n"

                + "void main(){\n"
                + "   vec4 color = texture(" + UNIFORM_TEXTURE0 + "," + VARYING_TEXCOORD + ");\n"
                + "   float grey = color.r * 0.299 + color.g * 0.587 + color.b * 0.114;"
                + "   fragColor = vec4(vec3(grey), 1.0);"
                + "}\n";
    }
}
