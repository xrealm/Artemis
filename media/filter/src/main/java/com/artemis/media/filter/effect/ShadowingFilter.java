package com.artemis.media.filter.effect;

import com.artemis.media.filter.filter.BasicDynamicFilter;

/**
 * Created by xrealm on 2020/7/10.
 */
public class ShadowingFilter extends BasicDynamicFilter {

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n" +
                "precision highp float;\n" +
                " in highp vec2 " + VARYING_TEXCOORD + ";\n" +
                " uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n" +
                " \n" +
                " uniform highp float " + UNIFORM_TIME + ";\n" +
                " out vec4 fragColor;"+
                " \n" +
                " void main() {\n" +
                "     highp vec2 uv = " + VARYING_TEXCOORD + ";\n" +
                "     float amount = 0.0;\n" +
                "     amount = (1.0 + sin(" + UNIFORM_TIME + "*6.0)) * 0.5;\n" +
                "     amount *= 1.0 + sin(" + UNIFORM_TIME + "*16.0) * 0.5;\n" +
                "     amount *= 1.0 + sin(" + UNIFORM_TIME + "*19.0) * 0.5;\n" +
                "     amount *= 1.0 + sin(" + UNIFORM_TIME + "*27.0) * 0.5;\n" +
                "     amount = pow(amount, 3.0);\n" +
                "     amount *= 0.05; highp vec3 col;\n" +
                "     highp vec4 colorR = texture(" + UNIFORM_TEXTURE0 + ",fract(vec2(uv.x+amount, uv.y)));\n" +
                "     highp vec4 colorG = texture(" + UNIFORM_TEXTURE0 + ",fract(uv));\n" +
                "     highp vec4 colorB = texture(" + UNIFORM_TEXTURE0 + ",fract(vec2(uv.x-amount, uv.y)));\n" +
                "     col.r = colorR.r; col.g = colorG.g; col.b = colorB.b;\n" +
                "     col *= (1.0 - amount * 0.5);\n" +
                "     fragColor = vec4(col, (colorR.a + colorG.a + colorB.a) / 3.0);\n" +
                " }";
    }
}
