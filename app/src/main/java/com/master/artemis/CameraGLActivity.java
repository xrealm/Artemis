package com.master.artemis;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.artemis.media.camera.api.ICameraPreviewView;
import com.artemis.media.camera.config.CameraConfig;
import com.artemis.media.camera.render.ISurfaceRenderer;
import com.artemis.media.camera.view.CameraPreviewView;
import com.artemis.media.filter.view.GLTextureView;
import com.master.artemis.camera.CameraPreviewer;
import com.master.artemis.util.ScreenOrientationHelper;

/**
 * Created by xrealm on 2020/6/25.
 */
public class CameraGLActivity extends AppCompatActivity {


    private CameraPreviewer cameraPreviewer;
    private GLTextureView glTextureView;
    private ISurfaceRenderer surfaceRenderer;
    private CameraConfig cameraConfig;
    private ScreenOrientationHelper orientationHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams();
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
        initCameraConfig();
        openPreview();
    }

    private void initCameraConfig() {
        cameraConfig = CameraConfig.obtainV2();
//        cameraConfig.previewVideoWidth = 1280;
//        cameraConfig.previewVideoHeight = 720;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
        unRegisterOrientationChanged();
    }

    private void stopPreview() {
        cameraPreviewer.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openPreview();
        registerOrientationChanged();
    }

    private void openPreview() {
        if (cameraPreviewer == null) {
            cameraPreviewer = new CameraPreviewer(this, glTextureView);
        }
        cameraPreviewer.startPreview(this);
    }

    private void registerOrientationChanged() {
        if (orientationHelper == null) {
            orientationHelper = new ScreenOrientationHelper(this);
            orientationHelper.setOrientationChangedListener(new ScreenOrientationHelper.OrientationChangedListener() {
                @Override
                public void orientationChanged(int angle) {
                    cameraConfig.setOrientation(angle);
                }
            });
        }
        if (orientationHelper.canDetectOrientation()) {
            orientationHelper.enable();
        } else {
            orientationHelper = null;
        }
    }

    private void unRegisterOrientationChanged() {
        if (orientationHelper != null) {
            orientationHelper.disable();
            orientationHelper = null;
        }
    }

    private void setWindowParams() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        float screenBrightness = attributes.screenBrightness;
        float value = 0.7f;
        if (screenBrightness < value) {
            attributes.screenBrightness = value;
            getWindow().setAttributes(attributes);
        }
    }
}
