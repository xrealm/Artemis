package com.artemis.media.filter.beauty;

import android.opengl.GLES30;

import com.artemis.media.filter.filter.MultiInputFilter;

/**
 * Created by xrealm on 2020/7/12.
 */
public class HCSmoothFilter extends MultiInputFilter {

    private int sharpenHandle;
    private int blurAlphaHandle;
    private int widthOffsetHandle;
    private int heightOffsetHandle;
    private int freqRangeValueHandle;
    private int freqRangeBlurHandle;

    private float widthOffset;
    private float heightOffset;
    
    private float smoothLevel;

    public HCSmoothFilter() {
        super(3);
    }

    public void setSmoothLevel(float level) {
        smoothLevel = level;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        blurAlphaHandle = GLES30.glGetUniformLocation(programHandle, "blurAlpha");
        widthOffsetHandle = GLES30.glGetUniformLocation(programHandle, "widthOffset");
        heightOffsetHandle = GLES30.glGetUniformLocation(programHandle, "heightOffset");
        sharpenHandle = GLES30.glGetUniformLocation(programHandle, "sharpen");
        freqRangeValueHandle = GLES30.glGetUniformLocation(programHandle, "frequencyRangeValue");
        freqRangeBlurHandle = GLES30.glGetUniformLocation(programHandle, "frequencyRangeBlur");
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        widthOffset = 1.0f / getWidth();
        heightOffset = 1.0f / getHeight();
        GLES30.glUniform3f(blurAlphaHandle, smoothLevel, 0, 0);
        GLES30.glUniform1f(widthOffsetHandle, widthOffset);
        GLES30.glUniform1f(heightOffsetHandle, heightOffset);
        GLES30.glUniform1f(sharpenHandle, 0.2f);
        GLES30.glUniform4f(freqRangeValueHandle, 0.2f, 0.5f, 0.9f, 0.9f);
        GLES30.glUniform4f(freqRangeBlurHandle, 1.0f, 0.9f, 0.5f, 0.08f);
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n" +
                "precision highp float;\n" +
                "in highp vec2 textureCoordinate; \n" +
                "uniform sampler2D inputImageTexture0; //原图\n" +
                "uniform sampler2D inputImageTexture1; // box blur \n" +
                "uniform sampler2D inputImageTexture2; // 高反差blur \n" +
                "uniform lowp vec3 blurAlpha; //0.59   1.0\n" +
                "uniform highp float widthOffset; // 1/w\n" +
                "uniform highp float heightOffset; // 1/h\n" +
                "uniform highp float sharpen;//0.05\n" +
                "\n" +
                "uniform  vec4 frequencyRangeValue; \n" +
                "uniform  vec4 frequencyRangeBlur;\n" +
                "out vec4 fragColor;\n" +
                "\n" +
                "void main() \n" +
                "{ \n" +
                "    lowp vec4 iColor = texture(inputImageTexture0, textureCoordinate); \n" +
                "    lowp vec4 meanColor = texture(inputImageTexture1, textureCoordinate); \n" +
                "    lowp vec4 varColor = texture(inputImageTexture2, textureCoordinate); \n" +
                "    lowp float theta = 0.1; \n" +
                "    mediump float p = clamp((min(iColor.r, meanColor.r - 0.1) - 0.2) * 4.0, 0.0, 1.0); \n" +
                "    mediump float meanVar = (varColor.r + varColor.g + varColor.b) / 3.0; \n" +
                "    mediump float kMin; \n" +
                "    lowp vec3 resultColor; \n" +
                "    lowp float intensity = blurAlpha.r; \n" +
                "    kMin = (1.0 - meanVar / (meanVar + theta)) * p * intensity; \n" +
                "\n" +
                "    ////////////////////////////////////////////////////////\n" +
                "    //分频\n" +
                "    if(kMin > 1.0 - frequencyRangeValue.x)\n" +
                "    {\n" +
                "        kMin = kMin * frequencyRangeBlur.x ; //低频\n" +
                "    }\n" +
                "    else if(kMin > 1.0 - frequencyRangeValue.y)\n" +
                "    {\n" +
                "        kMin = kMin * frequencyRangeBlur.y; //低中频\n" +
                "    }\n" +
                "    else if(kMin > 1.0 - frequencyRangeValue.z)\n" +
                "    {\n" +
                "        kMin = kMin * frequencyRangeBlur.z; //中频\n" +
                "    }\n" +
                "    else\n" +
                "    {\n" +
                "        kMin = kMin * frequencyRangeBlur.w; //高频        \n" +
                "    }    \n" +
                "\n" +
                "    ////////////////////////////////////////////////////////\n" +
                "\n" +
                "    resultColor = mix(iColor.rgb, meanColor.rgb, kMin); \n" +
                "     \n" +
                "    highp float sum = 0.25*iColor.g;\n" +
                "    sum += 0.125*texture(inputImageTexture0,textureCoordinate+vec2(-widthOffset,0.0)).g;\n" +
                "    sum += 0.125*texture(inputImageTexture0,textureCoordinate+vec2(widthOffset,0.0)).g;\n" +
                "    sum += 0.125*texture(inputImageTexture0,textureCoordinate+vec2(0.0,-heightOffset)).g;\n" +
                "    sum += 0.125*texture(inputImageTexture0,textureCoordinate+vec2(0.0,heightOffset)).g;\n" +
                "    sum += 0.0625*texture(inputImageTexture0,textureCoordinate+vec2(widthOffset,heightOffset)).g;\n" +
                "    sum += 0.0625*texture(inputImageTexture0,textureCoordinate+vec2(-widthOffset,-heightOffset)).g;\n" +
                "    sum += 0.0625*texture(inputImageTexture0,textureCoordinate+vec2(-widthOffset,heightOffset)).g;\n" +
                "    sum += 0.0625*texture(inputImageTexture0,textureCoordinate+vec2(widthOffset,-heightOffset)).g;\n" +
                "\n" +
                "\n" +
                "    float hPass = iColor.g-sum+0.5;\n" +
                "    float flag = step(0.5, hPass);\n" +
                "    highp vec3 color = mix(max(vec3(0.0), (2.0*hPass + resultColor - 1.0)), min(vec3(1.0), (resultColor + 2.0*hPass - 1.0)), flag);\n" +
                "    color = mix(resultColor.rgb, color.rgb, sharpen);\n" +
                "\n" +
                "    fragColor = vec4(color, 1.0);\n" +
                "}";
    }
}
