package com.master.artemis;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.artemis.imageprocess.ImagePreviewer;
import com.artemis.media.filter.view.GLTextureView;

public class ImageGLActivity extends AppCompatActivity {

    private GLTextureView previewView;
    private ImagePreviewer imagePreviewer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        previewView = findViewById(R.id.image_preview_texture_view);
        imagePreviewer = new ImagePreviewer(getApplicationContext(), previewView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        imagePreviewer.startRenderer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        imagePreviewer.releaseRender();
    }
}
