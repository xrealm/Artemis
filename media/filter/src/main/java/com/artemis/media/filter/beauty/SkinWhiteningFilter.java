package com.artemis.media.filter.beauty;

import android.opengl.GLES30;

import com.artemis.media.filter.R;
import com.artemis.media.filter.filter.BasicFilter;
import com.artemis.media.filter.input.GLTextureOutputRenderer;
import com.artemis.media.filter.util.ImageHelper;
import com.artemis.media.filter.util.TextureHelper;

/**
 * Created by xrealm on 2020/7/12.
 */
public class SkinWhiteningFilter extends BasicFilter {

    private static final String UNIFORM_ALPHA = "alpha";
    private static final String UNIFORM_LEVEL_BLACK = "levelBlack";
    private static final String UNIFORM_LEVEL_RANGE_INV = "levelRangeInv";

    private int alphaHandle;
    private int levelBlackHandle;
    private int levelRangeInvHandle;
    private int lookupTextureHandle;
    private int lookupGrayTextureHandle;

    private int lookupTexture;
    private int lookupGrayTexture;

    private float alpha;

    public void setWhiteningLevel(float level) {
        alpha = level;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        alphaHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_ALPHA);
        levelBlackHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_LEVEL_BLACK);
        levelRangeInvHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_LEVEL_RANGE_INV);

        lookupTextureHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_TEXTUREBASE + 1);
        lookupGrayTextureHandle = GLES30.glGetUniformLocation(programHandle, UNIFORM_TEXTUREBASE + 2);
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        if (lookupTexture == 0 || lookupGrayTexture == 0) {
            return;
        }
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lookupTexture);
        GLES30.glUniform1i(lookupTextureHandle, 1);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lookupGrayTexture);
        GLES30.glUniform1i(lookupGrayTextureHandle, 2);

        GLES30.glUniform1f(alphaHandle, alpha);
        GLES30.glUniform1f(levelBlackHandle, 0);
        GLES30.glUniform1f(levelRangeInvHandle, 0);
    }

    @Override
    public void newTextureReady(int texture, GLTextureOutputRenderer source, boolean newData) {
        if (lookupTexture == 0) {
            lookupTexture = TextureHelper.bitmapToTexture(ImageHelper.decodeBitmap(R.mipmap.artemis_beauty_color_enhancement_lookup));
        }
        if (lookupGrayTexture == 0) {
            lookupGrayTexture = TextureHelper.bitmapToTexture(ImageHelper.decodeBitmap(R.mipmap.artemis_beauty_color_enhancement_curve));
        }
        super.newTextureReady(texture, source, newData);
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n" +
                "precision highp float;\n" +
                "uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n" + // source color
                "in vec2 " + VARYING_TEXCOORD + ";\n" +
                "uniform sampler2D " + UNIFORM_TEXTUREBASE + 1 + ";\n" + // lookup
                "uniform sampler2D " + UNIFORM_TEXTUREBASE + 2 + ";\n" + // lookupGray
                "uniform float levelBlack;\n" +
                "uniform float levelRangeInv;\n" +
                "uniform float alpha;\n" +
                "out vec4 fragColor;\n" +
                "void main() {\n" +
                "    vec3 colorOrigin = texture(" + UNIFORM_TEXTURE0 + "," + VARYING_TEXCOORD + ").rgb;\n" +
                "    vec3 color = clamp((colorOrigin - vec3(levelBlack)) * levelRangeInv, 0.0, 1.0);\n" +
                "    vec3 texel;\n" +
                "    texel.r = texture(" + UNIFORM_TEXTUREBASE + 2 + ", vec2((color.r * 255.0 + 0.5) / 256.0, 0.5)).r;\n" +
                "    texel.g = texture(" + UNIFORM_TEXTUREBASE + 2 + ", vec2((color.g * 255.0 + 0.5) / 256.0, 0.5)).g;\n" +
                "    texel.b = texture(" + UNIFORM_TEXTUREBASE + 2 + ", vec2((color.b * 255.0 + 0.5) / 256.0, 0.5)).b;\n" +
                "    texel = mix(color, texel, 0.5);\n" +
                "    texel = mix(colorOrigin, texel, alpha);\n" +
                "    \n" +
                "    float blueColor = texel.b * 15.0;\n" +
                "    vec2 quad1, quad2;\n" +
                "    quad1.y = floor(floor(blueColor) * 0.25);\n" +
                "    quad1.x = floor(blueColor) - (quad1.y * 4.0);\n" +
                "    quad2.y = floor(ceil(blueColor) * 0.25);\n" +
                "    quad2.x = ceil(blueColor) - (quad2.y * 4.0);\n" +
                "    vec2 texPos2, texPos1;\n" +
                "    texPos2 = texel.rg * 0.234375 + 0.0078125;\n" +
                "    texPos1 = quad1 * 0.25 + texPos2;\n" +
                "    texPos2 = quad2 * 0.25 + texPos2;\n" +
                "    vec4 newColor1 = texture(" + UNIFORM_TEXTUREBASE + 1 + ", texPos1);\n" +
                "    vec4 newColor2 = texture(" + UNIFORM_TEXTUREBASE + 1 + ", texPos2);\n" +
                "    color = mix(newColor1.rgb, newColor2.rgb, fract(blueColor));\n" +
                "    color = mix(texel, color, 0.75);\n" +
                "    fragColor = vec4(mix(colorOrigin, color, alpha), 1.0);\n" +
                "}";
    }
}
