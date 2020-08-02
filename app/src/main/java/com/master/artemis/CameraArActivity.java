package com.master.artemis;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.artemis.media.camera.api.ICameraPreviewView;
import com.artemis.media.camera.view.CameraPreviewView;
import com.artemis.media.filter.view.GLTextureView;
import com.master.artemis.camera.ArCorePreviewer;

/**
 * Created by xrealm on 2020/7/30.
 */
public class CameraArActivity extends AppCompatActivity {

    private GLTextureView glTextureView;
    private ArCorePreviewer previewer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_ar);

        ViewGroup rootView = findViewById(R.id.cam_preview_rootview);

        final CameraPreviewView previewView = new CameraPreviewView(this, ICameraPreviewView.VIEW_GL_TEXTURE);

        rootView.addView(previewView);
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

    }

    private void openPreview() {
        if (previewer == null) {
            previewer = new ArCorePreviewer(this, glTextureView);
        }
        previewer.startPreview(this);
    }

    private void stopPreview() {
        previewer.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
    }
}
