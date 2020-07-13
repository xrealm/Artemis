package com.artemis.player.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

public class ArtemisSensorListener implements SensorEventListener {
    private static final String TAG = "[ArtemisSensor]";

    private SensorManager sensorManager;
    private int motionDelay = SensorManager.SENSOR_DELAY_GAME;
    private final Object matrixLock = new Object();
    private boolean isStarted = false;
    private float[] sensorMatrix = new float[16];
    private float[] tmpMatrix = new float[16];

    private Context context;

    private UpdateSensorListener updateSensorListener;

    private Runnable updateSensorRunnable = new Runnable() {
        @Override
        public void run() {
            // tmpMatrix will be used in multi thread.
            synchronized (matrixLock){

            }
        }
    };

    public ArtemisSensorListener(Context context) {
        this.context = context.getApplicationContext();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void setUpdateSensorListener(UpdateSensorListener listener) {
        this.updateSensorListener = listener;
    }

    public boolean start() {
        if(isStarted) {
            return true;
        }

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(sensor == null){
            Log.i(TAG, "sensor do not support TYPE_ROTATION_VECTOR");
            return false;
        }

        boolean isStarted = sensorManager.registerListener(this, sensor, motionDelay, null);
        if (!isStarted) {
            sensorManager.unregisterListener(this);
        }
        this.isStarted = isStarted;
        return isStarted;
    }

    public void stop() {
        if (isStarted) {
            sensorManager.unregisterListener(this);
            isStarted = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == 0) {
            return;
        }
        int deviceRotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
//                ArtemisSensorListener.sensorRotationVector2Matrix(event.values, deviceRotation, sensorMatrix);
                // tmpMatrix will be used in multi thread.
//                synchronized (matrixLock){
//                    System.arraycopy(sensorMatrix, 0, tmpMatrix, 0, 16);
//                }
//                if (updateSensorListener != null) {
//                    updateSensorListener.updateSensor(updateSensorRunnable);
//                }

                float[] values = new float[]{event.values[0], event.values[1], event.values[2]};
                ArtemisSensorListener.sensorRotationVector2Matrix(values, deviceRotation, sensorMatrix);
                float[] output = new float[16];
                System.arraycopy(sensorMatrix, 0, output, 0, 16);
                if (updateSensorListener != null) {
                    updateSensorListener.updateSensor(output);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "onSensorChanged: " + accuracy);
    }

    private static float[] sTmp = new float[16];
    private static void sensorRotationVector2Matrix(float[] values, int rotation, float[] output) {
        switch (rotation){
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                SensorManager.getRotationMatrixFromVector(output, values);
                break;
            case Surface.ROTATION_90:
                SensorManager.getRotationMatrixFromVector(sTmp, values);
                SensorManager.remapCoordinateSystem(sTmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, output);
                break;
            case Surface.ROTATION_270:
                SensorManager.getRotationMatrixFromVector(sTmp, values);
                SensorManager.remapCoordinateSystem(sTmp, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, output);
                break;
        }
        Matrix.rotateM(output, 0, 90.0F, 1.0F, 0.0F, 0.0F);
    }

    public interface UpdateSensorListener {
        void updateSensor(float[] sensorMatrix);
    }

}
