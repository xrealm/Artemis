package com.artemis.media.filter.effect;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.SystemClock;

import com.artemis.media.filter.filter.BasicDynamicFilter;

/**
 * Created by xrealm on 2020/7/12.
 */
public class BarreBlurFilter extends BasicDynamicFilter {

    public static final String AMOUNT = "amount";
    private int amountHandle;
    private float amount = 0.06f;

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        amountHandle = GLES30.glGetUniformLocation(programHandle, AMOUNT);
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        GLES20.glUniform1f(amountHandle, amount);
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n" +
                "precision highp float;\n" +
                " in highp vec2 " + VARYING_TEXCOORD + ";\n" +
                " uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n" +
                " uniform highp float " + AMOUNT + ";\n" +
                " uniform highp float " + UNIFORM_TIME + ";\n" +
                " const float gamma = 2.2;\n" +
                " out vec4 fragColor;" +
                " vec2 barrelDistortion (vec2 p, vec2 amt){\n" +
                "     p = 2.0*p-1.0; float BarrelPower = 1.125;\n" +
                "     float maxBarrelPower = 3.0;\n" +
                "     float theta = atan(p.y,p.x);\n" +
                "     float radius = length(p);\n" +
                "     radius = pow(radius,1.0+maxBarrelPower*amt.x);\n" +
                "     p.x = radius * cos(theta);\n" +
                "     p.y = radius * sin(theta);\n" +
                "     return 0.5 *(p+1.0);\n" +
                " }\n" +
                " float sat(float t){\n" +
                "     return clamp(t,0.0,1.0);\n" +
                " }\n" +
                " float linterp(float t){\n" +
                "     return sat(1.0-abs(2.0*t - 1.0));\n" +
                " }\n" +
                " float remap(float t,float a,float b){\n" +
                "     return sat((t-a)/(b-a));\n" +
                " }\n" +
                " vec3 spectrum_offset(float t){\n" +
                "     vec3 ret; float lo = step(t,0.5);\n" +
                "     float hi = 1.0-lo;\n" +
                "     float w = linterp(remap(t,1.0/6.0,5.0/6.0));\n" +
                "     ret = vec3(lo,1.0,hi) * vec3(1.0-w,w,1.0-w);\n" +
                "     return pow(ret,vec3(1.0/2.2));\n" +
                " }\n" +
                " float nrand(vec2 n){\n" +
                "     return fract(sin(dot(n.xy,vec2(12.9898,78.233))) * 43758.5453);\n" +
                " }\n" +
                " vec3 lin2srgb(vec3 c){\n" +
                "     return pow(c,vec3(gamma));\n" +
                " }\n" +
                " vec3 srgb2lin(vec3 c){\n" +
                "     return pow(c,vec3(1.0/gamma));\n" +
                " }\n" +
                " void main(){\n" +
                "     int num_iter = 16;\n" +
                "     highp float reci_num_iter_f = 1.0/float(num_iter);\n" +
                "     highp float MAX_DIST_PX = 200.0;\n" +
                "     highp vec2 uv = " + VARYING_TEXCOORD + ";\n" +
                "     highp vec4 origCol = texture(" + UNIFORM_TEXTURE0 + ",fract(uv)).rgba;\n" +
                "     highp vec2 max_distort = vec2(" + AMOUNT + ");\n" +
                "     highp vec2 oversiz = barrelDistortion(vec2(1.0,1.0),max_distort);\n" +
                "     highp vec2 tmp = 2.0 * uv -1.0;\n" +
                "     tmp = tmp/(oversiz*oversiz);\n" +
                "     tmp = 0.5*tmp + 0.5;\n" +
                "     highp vec3 sumcol = vec3(0.0);\n" +
                "     highp vec3 sumw = vec3(0.0);\n" +
                "     highp float rnd = nrand(tmp + fract(" + 1.0 + "));\n" +
                "     for(int i=0;i<num_iter;i++){\n" +
                "         highp float t = (float(i)+rnd) * reci_num_iter_f;\n" +
                "         highp vec3 w = spectrum_offset(t);\n" +
                "         sumw+=w;\n" +
                "         sumcol +=w*srgb2lin(texture(" + UNIFORM_TEXTURE0 + ",fract(barrelDistortion(tmp,max_distort*t))).rgb);\n" +
                "     }\n" +
                "     sumcol.rgb/=sumw;\n" +
                "     highp vec3 outcol = lin2srgb(sumcol.rgb);\n" +
                "     outcol+=rnd/255.0;\n" +
                "     fragColor = vec4(outcol,1.0);\n" +
                " }";
    }
}
