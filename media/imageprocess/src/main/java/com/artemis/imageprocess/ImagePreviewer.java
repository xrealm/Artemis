package com.artemis.imageprocess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.artemis.media.filter.FastImageProcessingPipeline;
import com.artemis.media.filter.GLScreenEndpoint;
import com.artemis.media.filter.input.ImageResourceInput;
import com.artemis.media.filter.view.GLTextureView;

/**
 * Created by xrealm on 2020/6/27.
 */
public class ImagePreviewer {


    private FastImageProcessingPipeline imagePipeline;
    private GLScreenEndpoint screenEndPoint;
    private ImageResourceInput imageInput;
    private GLTextureView glView;

    private int imageWidth;
    private int imageHeight;


    public ImagePreviewer(Context context, GLTextureView glView) {
        this.glView = glView;
        this.glView.setEGLContextClientVersion(2);
        imagePipeline = new FastImageProcessingPipeline();
        this.glView.setRenderer(imagePipeline);
        this.glView.setRenderMode(GLTextureView.RENDERMODE_WHEN_DIRTY);

        screenEndPoint = new GLScreenEndpoint();
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.test_texture);
        imageWidth = bitmap.getWidth();
        imageHeight = bitmap.getHeight();
        imageInput = new ImageResourceInput(glView, bitmap);
        imagePipeline.addRootRenderer(imageInput);

        imageInput.addTarget(screenEndPoint);

    }

    public void startRenderer() {
        imagePipeline.startRendering();
    }

    public void releaseRender() {
        imagePipeline.destroy();
    }
}
