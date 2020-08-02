package com.master.artemis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.artemis.ar.ArCoreCapacity;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_ALL = 10000;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoTestActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.tv_camera_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraGLActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.tv_player_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoTestActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.tv_ar_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraArActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.tv_image_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageGLActivity.class);
                startActivity(intent);
            }
        });

        requestAppPermission();
        requestArCore();
    }

    private ArCoreCapacity arCoreCapacity = new ArCoreCapacity();

    private void requestArCore() {
        arCoreCapacity.setArCoreAvailabilityListener(new ArCoreCapacity.IArCoreAvailabilityListener() {
            @Override
            public void onAvailability(boolean isSupported) {
                findViewById(R.id.tv_ar_preview).setVisibility(isSupported ? View.VISIBLE : View.INVISIBLE);
            }
        });
        arCoreCapacity.checkAvailability(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ALL) {
            boolean success = permissions.length == 0 || verifyPermissions(grantResults);
            if (!success) {
                Toast.makeText(this, "缺少权限无法正常使用", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestAppPermission() {
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        };
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQ_ALL);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
