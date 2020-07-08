package com.master.artemis;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.Surface;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.artemis.media.camera.api.ICameraPreviewView;
import com.artemis.media.camera.config.CameraConfig;
import com.artemis.media.camera.render.ISurfaceRenderer;
import com.artemis.media.camera.view.CameraPreviewView;
import com.artemis.media.filter.view.GLTextureView;
import com.master.artemis.camera.CameraPreviewer;

/**
 * Created by xrealm on 2020/6/25.
 */
public class CameraGLActivity extends AppCompatActivity {


    private CameraPreviewer cameraPreviewer;
    private GLTextureView glTextureView;
    private ISurfaceRenderer surfaceRenderer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        FrameLayout rootView = findViewById(R.id.cam_preview_rootview);

        final CameraPreviewView previewView = new CameraPreviewView(this, ICameraPreviewView.VIEW_GL_TEXTURE);

        rootView.addView(previewView);
        surfaceRenderer = previewView.getSurfaceRenderer();
        previewView.setAspectRatio(720, 1280);
        previewView.addSurfaceCallback(new ICameraPreviewView.ISurfaceCallback() {
            @Override
            public void onSurfaceAvailable(Object surface) {
                if (surface instanceof SurfaceTexture) {
                    ((SurfaceTexture) surface).setDefaultBufferSize(720, 1280);
                }
            }

            @Override
            public void onSurfaceDestroyed(Object surface) {

            }

            @Override
            public void onSurfaceChanged(Object surface, int width, int height) {

            }
        });
        glTextureView = (GLTextureView) previewView.getSurfaceRenderer();
        openPreview();

    }

    private int getRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                return 0;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
    }

    private void stopPreview() {
        cameraPreviewer.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openPreview();
    }

    private void openPreview() {
        CameraConfig cameraConfig = CameraConfig.obtain();
        if (cameraPreviewer == null) {
            cameraPreviewer = new CameraPreviewer(this, glTextureView);
        }
        cameraPreviewer.startPreview(this);
    }
}
