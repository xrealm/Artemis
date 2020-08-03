package com.master.artemis.camera;

import android.app.Activity;

import com.artemis.media.camera.config.ArCoreConfig;
import com.artemis.media.camera.input.ArCorePreviewFilter;
import com.artemis.media.filter.FastImageProcessingPipeline;
import com.artemis.media.filter.GLScreenEndpoint;
import com.artemis.media.filter.view.GLTextureView;

/**
 * Created by xrealm on 2020/8/1.
 */
public class ArCorePreviewer {
    private FastImageProcessingPipeline mImagePipeline;
    private GLScreenEndpoint mScreenEndPoint;
    private ArCorePreviewFilter mArPreviewFilter;
    private GLTextureView mGLView;

    private DisplayRotationHelper mDisplayRotationHelper;

    public ArCorePreviewer(Activity activity, GLTextureView textureView, ArCoreConfig arCoreConfig) {
        mGLView = textureView;
        mGLView.setEGLContextClientVersion(3);

        mImagePipeline = new FastImageProcessingPipeline();
        mGLView.setRenderer(mImagePipeline);
        mGLView.setRenderMode(GLTextureView.RENDERMODE_CONTINUOUSLY);

        mScreenEndPoint = new GLScreenEndpoint();
        mArPreviewFilter = new ArCorePreviewFilter(activity.getApplicationContext(), arCoreConfig);
        mArPreviewFilter.setRenderSize(720,1280);

        mArPreviewFilter.addTarget(mScreenEndPoint);
        mImagePipeline.addRootRenderer(mArPreviewFilter);

        mDisplayRotationHelper = new DisplayRotationHelper(activity.getApplicationContext());
    }

    public void onResume() {
        mDisplayRotationHelper.onResume();
    }

    public void onPause() {
        mDisplayRotationHelper.onPause();
    }

    public void startPreview(Activity activity) {
        mArPreviewFilter.startPreview();
        startRenderer();
    }

    private void startRenderer() {
        mImagePipeline.startRendering();
    }

    public void destroy() {
        mArPreviewFilter.stopPreview();
        mImagePipeline.destroy();
    }
}
