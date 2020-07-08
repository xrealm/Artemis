package com.artemis.media.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Range;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.artemis.media.camera.config.CameraConfig;
import com.artemis.media.camera.log.CamLog;
import com.artemis.media.camera.util.Camera2Util;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Camera2 实现类
 * Created by xrealm on 2020/6/25.
 */
public class Camera2Impl implements ICamera {

    private static final String TAG = "[Camera2Impl.java]";

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private int mCameraCnt;
    private int mCameraRotation;
    private int mCameraId;
    private int mState = STATE_PREVIEW;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private HandlerThread mCameraWorkThread;
    private Handler mCameraHandler;

    private ICameraDataCallback mCameraDataCallback;
    private ImageReader mImageReader;
    private SurfaceTexture mCameraTexture;
    private byte[] mByteBuffer;

    private boolean mIsStartPreview = false;
    private CameraConfig mParameter;
    private Context mContext;

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private ImageReader.OnImageAvailableListener mImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            if (mCameraDataCallback != null) {
                mByteBuffer = Camera2Util.yuv420888toNv21(image, mByteBuffer);
                mCameraDataCallback.onData(mByteBuffer);
            }
            image.close();
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
                default:
                    break;
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

    public Camera2Impl(Context context, CameraConfig cameraConfig) {
        mContext = context;
        mParameter = cameraConfig;
    }

    private void createCameraPreviewSession() {
        mCameraTexture.setDefaultBufferSize(mParameter.getPreviewSize().getWidth(), mParameter.getPreviewSize().getHeight());
//        Surface surface = new Surface(mCameraTexture);
        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
//            mPreviewRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
//            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (mCameraDevice == null) {
                        return;
                    }

                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = session;
                    try {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                                new Range<>(mParameter.previewMinFps, mParameter.previewMaxFps));
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    session.close();
                    mCaptureSession = null;
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean openCamera(int degrees) {
        mCameraId = mParameter.getFacingId();
        startWorkThread();
        setUpCameraOrientation(degrees);
        setUpCameraOutputs();

        return true;
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (mCaptureSession != null) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void setUpCameraOrientation(int degrees) {
        CameraCharacteristics characteristics = getCameraCharacteristics();

        if (characteristics == null) {
            CamLog.e(TAG, "setUpCameraOrientation: characteristics is null.");
            return;
        }
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        int displayOrientation = 0;
        if (isFrontCamera()) {
            displayOrientation = (sensorOrientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (sensorOrientation - degrees + 360) % 360;
        }
        int rotation;
        switch (displayOrientation) {
            case Surface.ROTATION_90:
                rotation = 1;
                break;
            case Surface.ROTATION_180:
                rotation = 2;
                break;
            case Surface.ROTATION_270:
                rotation = 3;
                break;
            default:
                rotation = 0;
                break;
        }
        mCameraRotation = rotation * 90;
    }

    private void setUpCameraOutputs() {
        CameraCharacteristics characteristics = getCameraCharacteristics();

        if (characteristics == null) {
            CamLog.e(TAG, "setUpCameraOutputs: characteristics is null.");
            return;
        }
        StreamConfigurationMap map = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            CamLog.e(TAG, "setUpCameraOutputs: StreamConfigurationMap is null.");
            return;
        }
        Size size = Camera2Util.chooseOptimalPreviewSize(characteristics, mParameter);
        int previewVideoWidth = size.getWidth();
        int previewVideoHeight = size.getHeight();

        if (mImageReader == null) {
            mImageReader = ImageReader.newInstance(previewVideoWidth, previewVideoHeight, ImageFormat.YUV_420_888, 3);
            mImageReader.setOnImageAvailableListener(mImageAvailableListener, mCameraHandler);
        }
    }



    private void startWorkThread() {
        if (mCameraWorkThread == null) {
            mCameraWorkThread = new HandlerThread("CameraWorker");
            mCameraWorkThread.start();
            mCameraHandler = new Handler(mCameraWorkThread.getLooper());
        }
    }

    private void stopWorkThread() {
        if (mCameraWorkThread == null) {
            return;
        }
        mCameraWorkThread.quitSafely();
        try {
            mCameraWorkThread.join();
            mCameraWorkThread = null;
            mCameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean startPreview(SurfaceTexture surfaceTexture) {
        mCameraTexture = surfaceTexture;
        if (mIsStartPreview) {
            return true;
        }
        CameraManager cameraManager = getCameraManager();
        try {
            Camera2Util.chooseOptimalFpsRange(getCameraCharacteristics(), mParameter);
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            cameraManager.openCamera(String.valueOf(mCameraId), mStateCallback, mCameraHandler);
            mIsStartPreview = true;
            return true;
        } catch (CameraAccessException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void stopPreview() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        mIsStartPreview = false;
    }

    @Override
    public boolean switchCamera(int degrees) {
        return false;
    }

    @Override
    public void pauseCamera() {
        stopPreview();
    }

    @Override
    public void resumeCamera() {

    }

    @Override
    public void release() {
        closeCamera();
        stopWorkThread();
    }

    @Override
    public boolean isFrontCamera() {
        return true;
    }

    @Override
    public void setCameraDataCallback(ICameraDataCallback callback) {
        mCameraDataCallback = callback;
    }

    @Override
    public int getCameraRotation() {
        return mCameraRotation = 90;
    }

    @Override
    public boolean isSupportFlashMode() {
        return false;
    }

    @Override
    public void setOnCameraErrorListener(OnCameraErrorListener lis) {

    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mCameraHandler);

            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mCameraHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        if (mCameraDevice == null) {
            return;
        }
        try {
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            // Orientation
//            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
//        if (mFlashSupported) {
//            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
//                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//        }
    }

    private CameraManager getCameraManager() {
        if (mCameraManager == null) {
            return (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        }
        return mCameraManager;
    }

    private CameraCharacteristics getCameraCharacteristics() {
        CameraManager cameraManager = getCameraManager();
        try {
            return cameraManager.getCameraCharacteristics(String.valueOf(mCameraId));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
