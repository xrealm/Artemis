package com.artemis.media.camera.input;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.util.Log;

import com.artemis.media.egl.util.GLUtil;
import com.artemis.media.filter.input.GLTextureOutputRenderer;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by xrealm on 2020/7/30.
 */
public class ArCorePreviewFilter extends GLTextureOutputRenderer {

    private static final String TAG = "[ArCorePreviewFilter]";

    /**
     * (-1, 1) ------- (1, 1)
     *   |    \           |
     *   |       \        |
     *   |          \     |
     *   |             \  |
     * (-1, -1) ------ (1, -1)
     * Ensure triangles are front-facing, to support glCullFace().
     * This quad will be drawn using GL_TRIANGLE_STRIP which draws two
     * triangles: v0->v1->v2, then v2->v1->v3.
     */
    private static final float[] QUAD_COORDS = new float[]{
            -1.0f, -1.0f, +1.0f, -1.0f, -1.0f, +1.0f, +1.0f, +1.0f,
    };

    public static final float[] QUAD_TEXCOORDS = new float[]{
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };


    private Session mSession;

    private int mCameraTextureId;
    private FloatBuffer mQuadCoords;
    private FloatBuffer mQuadTexCoords;

    private Context mContext;


    public ArCorePreviewFilter(Context context) {
        mContext = context;
        initCoords();
    }

    private void initCoords() {
        mQuadCoords = ByteBuffer.allocateDirect(QUAD_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mQuadCoords.put(QUAD_COORDS);
        mQuadCoords.position(0);
        mQuadTexCoords = ByteBuffer.allocateDirect(QUAD_TEXCOORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mQuadTexCoords.put(QUAD_TEXCOORDS);
        mQuadTexCoords.position(0);
    }

    public void startPreview() {

        if (mSession == null) {
            openSession();
            Config config = mSession.getConfig();
            if (mSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                config.setDepthMode(Config.DepthMode.AUTOMATIC);
            } else {
                config.setDepthMode(Config.DepthMode.DISABLED);
            }
            mSession.configure(config);
        }
        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
            mSession = null;
        }

    }

    public void stopPreview() {
        if (mSession != null) {
            mSession.pause();
        }
    }

    @Override
    public void onDrawFrame() {
        markAsDirty();

        super.onDrawFrame();
    }

    @Override
    public void drawSub() {
        GLES30.glViewport(0, 0, getWidth(), getHeight());
        GLES30.glClearColor(getBackgroundRed(), getBackgroundGreen(), getBackgroundBlue(), getBackgroundAlpha());
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);

        if (mSession == null) {
            return;
        }

        if (mCameraTextureId == 0) {
            mCameraTextureId = GLUtil.generateOESTexture();
            mSession.setCameraTextureName(mCameraTextureId);
            mSession.setDisplayGeometry(0, getWidth(), getHeight());
        }
        Frame frame = null;
        try {
            frame = mSession.update();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (frame == null) {
            return;
        }
        Camera camera = frame.getCamera();
        drawBackground(frame);


//        // Get projection matrix.
//        float[] projmtx = new float[16];
//        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
//
//        // Get camera matrix and draw.
//        float[] viewmtx = new float[16];
//        camera.getViewMatrix(viewmtx, 0);
    }

    private void drawBackground(Frame frame) {
        if (frame == null) {
            return;
        }
        if (frame.hasDisplayGeometryChanged()) {
            frame.transformCoordinates2d(
                    Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                    mQuadCoords,
                    Coordinates2d.TEXTURE_NORMALIZED,
                    mQuadTexCoords);
        }
        GLES30.glUseProgram(programHandle);

        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthMask(false);

        GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 8, mQuadCoords);
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 8, mQuadTexCoords);
        GLES30.glEnableVertexAttribArray(texCoordHandle);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCameraTextureId);
        GLES30.glUniform1i(textureHandle, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glDisableVertexAttribArray(positionHandle);
        GLES30.glDisableVertexAttribArray(texCoordHandle);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        // TODO: 2020/8/2  GL_DEPTH_TEST
        // Restore the depth state for further drawing.
        GLES30.glDepthMask(true);
//        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
    }

    private boolean openSession() {
        Exception exception = null;
        String message = null;
        try {
            mSession = new Session(mContext);
        } catch (UnavailableArcoreNotInstalledException e) {
            message = "Please install ARCore";
            exception = e;
        } catch (UnavailableApkTooOldException e) {
            message = "Please update ARCore";
            exception = e;
        } catch (UnavailableSdkTooOldException e) {
            message = "Please update this app";
            exception = e;
        } catch (UnavailableDeviceNotCompatibleException e) {
            message = "This device does not support AR";
            exception = e;
        } catch (Exception e) {
            message = "Failed to create AR session";
            exception = e;
        }

        if (message != null) {
            Log.e(TAG, "Exception creating session", exception);
            return false;
        }
        return true;
    }

    @Override
    protected String getVertexShader() {
        return "#version 300 es\n"
                + "layout(location = 0) in vec4 " + ATTRIBUTE_POSITION + ";\n"
                + "layout(location = 1) in vec2 " + ATTRIBUTE_TEXCOORD + ";\n" +
                "out vec2 " + VARYING_TEXCOORD + ";\n" +
                "void main() {\n" +
                "    gl_Position = " + ATTRIBUTE_POSITION + ";\n" +
                "    " + VARYING_TEXCOORD + " = " + ATTRIBUTE_TEXCOORD + ";\n" +
                "}";
    }

    @Override
    protected String getFragmentShader() {
        return "#version 300 es\n" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "in vec2 " + VARYING_TEXCOORD + ";\n" +
                "out vec4 fragColor;\n" +
                "uniform samplerExternalOES " + UNIFORM_TEXTURE0 + ";\n" +
                "void main() {\n" +
                "    fragColor = texture(" + UNIFORM_TEXTURE0 + "," + VARYING_TEXCOORD + ");\n" +
                "}";

    }
}
