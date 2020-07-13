package com.artemis.media.filter.processing;

import com.artemis.media.filter.filter.MultiInputFilter;

/**
 * Created by xrealm on 2020/7/10.
 */
public class HighContrastFilter extends MultiInputFilter {

    public HighContrastFilter() {
        super(2);
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n"

                + "uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n"
                + "uniform sampler2D " + UNIFORM_TEXTUREBASE + 1 + ";\n"
                + "in highp vec2 " + VARYING_TEXCOORD + ";\n"
                + "out mediump vec4 fragColor;\n"

                + "void main(){\n"
                + "   lowp vec3 sourceColor = texture(" + UNIFORM_TEXTURE0 + "," + VARYING_TEXCOORD + ").rgb;\n"
                + "   lowp vec3 meanColor = texture(" + UNIFORM_TEXTUREBASE + 1 + "," + VARYING_TEXCOORD + ").rgb;\n"
                + "   highp vec3 diffColor = (sourceColor - meanColor) * 7.07;\n"
                + "   diffColor = min(diffColor * diffColor, 1.0);\n"
                + "   fragColor = vec4(diffColor, 1.0);\n"
                + "}\n";
    }

}
