package com.master.artemis.camera;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

import com.artemis.media.camera.config.CameraConfig;
import com.artemis.media.camera.filter.ImagePreProcessingFilter;
import com.artemis.media.camera.input.CameraPreviewInput;
import com.artemis.media.filter.FastImageProcessingPipeline;
import com.artemis.media.filter.GLScreenEndpoint;
import com.artemis.media.filter.view.GLTextureView;

/**
 * Created by xrealm on 2020/6/27.
 */
public class CameraPreviewer implements TextureView.SurfaceTextureListener {

    private FastImageProcessingPipeline mImagePipeline;
    private GLScreenEndpoint mScreenEndPoint;
    private CameraPreviewInput mCameraPreviewInput;
    private ImagePreProcessingFilter preFilter;
    private GLTextureView mGLView;



    public CameraPreviewer(Activity activity, GLTextureView textureView) {
        mGLView = textureView;
        mGLView.setEGLContextClientVersion(3);

        CameraConfig cameraConfig = CameraConfig.obtain();
        mImagePipeline = new FastImageProcessingPipeline();
        mGLView.setRenderer(mImagePipeline);
        mGLView.setRenderMode(GLTextureView.RENDERMODE_WHEN_DIRTY);

        mScreenEndPoint = new GLScreenEndpoint();
        mCameraPreviewInput = new CameraPreviewInput(activity.getApplicationContext(), cameraConfig);
        mCameraPreviewInput.setRenderer(textureView);
        preFilter = new ImagePreProcessingFilter(activity.getApplicationContext());

        mCameraPreviewInput.addTarget(preFilter);
        preFilter.addTarget(mScreenEndPoint);

        mImagePipeline.addRootRenderer(mCameraPreviewInput);

    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startRenderer();
        surface.setDefaultBufferSize(720, 1280);
    }

    public void startPreview(Activity activity) {
        mCameraPreviewInput.startPreview(activity);
        startRenderer();
    }

    private void startRenderer() {
        mImagePipeline.startRendering();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void destroy() {
        mCameraPreviewInput.stopPreview();
        mImagePipeline.destroy();
    }
}
