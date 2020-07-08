package com.artemis.media.filter.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;

import com.artemis.media.filter.input.GLTextureOutputRenderer;
import com.artemis.media.filter.util.TextureHelper;

/**
 * Created by xrealm on 2020/7/5.
 */
public class LookupFilter extends BasicFilter {

    private Bitmap lutBitmap;
    private int lutTexture;
    private int lutTextureHandle;
    private int intensityHandle;

    public LookupFilter(Context context, int resId) {

        lutBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        lutTextureHandle = GLES30.glGetUniformLocation(programHandle, "inputImageTexture1");
        intensityHandle = GLES30.glGetUniformLocation(programHandle, "intensity");
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        GLES30.glUniform1f(intensityHandle, 1f);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lutTexture);
        GLES30.glUniform1i(lutTextureHandle, 1);
    }

    @Override
    protected String getFragmentShader() {
        return
                "#version 300 es\n"
                        + "precision mediump float;\n"
                        + "uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n"
                        + "uniform sampler2D " + UNIFORM_TEXTUREBASE + 1 + ";\n"
                        + "out vec4 fragColor;\n"
                        + "in vec2 " + VARYING_TEXCOORD + ";\n"
                        + "uniform float intensity;\n"

                        + "void main(){\n"
                        + "  vec4 texColour = texture(" + UNIFORM_TEXTURE0 + "," + VARYING_TEXCOORD + ");\n"
                        + "  float blueColor = texColour.b * 63.0;\n"
                        + "  vec2 quad1;\n"
                        + "  quad1.y = floor(floor(blueColor) / 8.0);\n"
                        + "  quad1.x = floor(blueColor) - (quad1.y * 8.0);\n"
                        + "  vec2 quad2;\n"
                        + "  quad2.y = floor(ceil(blueColor) / 8.0);\n"
                        + "  quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n"
                        + "  vec2 texPos1;\n"
                        + "  texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * texColour.r);\n"
                        + "  texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * texColour.g);\n"
                        + "  vec2 texPos2;\n"
                        + "  texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * texColour.r);\n"
                        + "  texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * texColour.g);\n"
                        + "  vec4 newColor1 = texture(" + UNIFORM_TEXTUREBASE + 1 + ", texPos1);\n"
                        + "  vec4 newColor2 = texture(" + UNIFORM_TEXTUREBASE + 1 + ", texPos2);\n"
                        + "  vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n"
                        + "  fragColor = mix(texColour, vec4(newColor.rgb, texColour.a), intensity);\n"
                        + "}\n";
    }

    @Override
    public void newTextureReady(int texture, GLTextureOutputRenderer source, boolean newData) {
        if (lutTexture == 0) {
            lutTexture = TextureHelper.bitmapToTexture(lutBitmap);
        }
        super.newTextureReady(texture, source, newData);
    }

    @Override
    public void releaseFrameBuffer() {
        super.releaseFrameBuffer();
        if (lutTexture != 0) {
            TextureHelper.deleteTexture(lutTexture);
            lutTexture = 0;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (lutTexture != 0) {
            TextureHelper.deleteTexture(lutTexture);
            lutTexture = 0;
        }
        if (lutBitmap != null && !lutBitmap.isRecycled()) {
            lutBitmap.recycle();
            lutBitmap = null;
        }
    }
}
