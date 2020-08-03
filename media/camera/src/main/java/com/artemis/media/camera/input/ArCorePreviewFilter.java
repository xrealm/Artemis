package com.artemis.media.camera.input;

import android.content.Context;
import android.media.Image;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.artemis.media.camera.config.ArCoreConfig;
import com.artemis.media.egl.util.GLUtil;
import com.artemis.media.filter.input.GLTextureOutputRenderer;
import com.artemis.media.filter.util.ShaderUtil;
import com.artemis.media.filter.util.TextureHelper;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
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

    private int mDepthTextureId;
    private int mCameraTextureId;
    private int mDepthProgramHandle;
    private int mDepthTextureHandle;
    private int mDepthPositionHandle;
    private int mDepthTexCoordHandle;
    private FloatBuffer mQuadCoords;
    private FloatBuffer mQuadTexCoords;

    private Context mContext;
    private ArCoreConfig mArCoreConfig;


    public ArCorePreviewFilter(Context context, ArCoreConfig arCoreConfig) {
        mContext = context;
        mArCoreConfig = arCoreConfig;
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
    protected void initWithGLContext() {
        super.initWithGLContext();
        mDepthProgramHandle = ShaderUtil.buildProgram(getVertexShader(), getDepthFragmentShader());
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        mDepthPositionHandle = GLES30.glGetAttribLocation(mDepthProgramHandle, ATTRIBUTE_POSITION);
        mDepthTexCoordHandle = GLES30.glGetAttribLocation(mDepthProgramHandle, ATTRIBUTE_TEXCOORD);
        mDepthTextureHandle = GLES30.glGetUniformLocation(mDepthProgramHandle, UNIFORM_TEXTUREBASE + 0);
    }

    @Override
    public void onDrawFrame() {
        markAsDirty();

        super.onDrawFrame();
    }

    @Override
    public void drawSub() {
        GLES30.glViewport(0, 0, getWidth(), getHeight());
        GLES30.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        if (mSession == null) {
            return;
        }
        if (mDepthTextureId == 0) {
            mDepthTextureId = TextureHelper.generateTexture();
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
        if (mSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
//            updateWidthDepthImage(frame);
        }
        drawBackground(frame, mArCoreConfig.isDepthColorVisualizationEnabled());


//        // Get projection matrix.
//        float[] projmtx = new float[16];
//        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
//
//        // Get camera matrix and draw.
//        float[] viewmtx = new float[16];
//        camera.getViewMatrix(viewmtx, 0);
    }

    private void updateWidthDepthImage(Frame frame) {
        try {
            Image depthImage = frame.acquireDepthImage();
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mDepthTextureId);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D,
                    0, GLES30.GL_RG8,
                    depthImage.getWidth(), depthImage.getHeight(),
                    0, GLES30.GL_RG, GLES30.GL_UNSIGNED_BYTE,
                    depthImage.getPlanes()[0].getBuffer());
            GLUtil.checkGLError(TAG, "BackgroundRendererDraw");
            depthImage.close();
        } catch (NotYetAvailableException e) {
            e.printStackTrace();
        }
    }

    private void drawBackground(Frame frame, boolean debugShowDepthMap) {
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
        if (frame.getTimestamp() == 0) {
            return;
        }

        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthMask(false);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

        if (debugShowDepthMap) {
            GLES30.glUseProgram(mDepthProgramHandle);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mDepthTextureId);
            GLES30.glUniform1i(mDepthTextureHandle, 0);

            GLES30.glVertexAttribPointer(mDepthPositionHandle, 2, GLES30.GL_FLOAT, false, 8, mQuadCoords);
            GLES30.glEnableVertexAttribArray(mDepthPositionHandle);
            GLES30.glVertexAttribPointer(mDepthTexCoordHandle, 2, GLES30.GL_FLOAT, false, 8, mQuadTexCoords);
            GLES30.glEnableVertexAttribArray(mDepthTexCoordHandle);
        } else {
            GLES30.glUseProgram(programHandle);
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCameraTextureId);
            GLES30.glUniform1i(textureHandle, 0);

            GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 8, mQuadCoords);
            GLES30.glEnableVertexAttribArray(positionHandle);
            GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 8, mQuadTexCoords);
            GLES30.glEnableVertexAttribArray(texCoordHandle);
        }

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        if (debugShowDepthMap) {
            GLES30.glDisableVertexAttribArray(mDepthPositionHandle);
            GLES30.glDisableVertexAttribArray(mDepthTexCoordHandle);
        } else {
            GLES30.glDisableVertexAttribArray(positionHandle);
            GLES30.glDisableVertexAttribArray(texCoordHandle);
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        // Restore the depth state for further drawing.
        GLES30.glDepthMask(true);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
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
    public void destroy() {
        super.destroy();
        if (mCameraTextureId != 0) {
            GLUtil.deleteTexture(mCameraTextureId);
            mCameraTextureId = 0;
        }
        if (mDepthTextureId != 0) {
            GLUtil.deleteTexture(mDepthTextureId);
            mDepthTextureId = 0;
        }
    }

    private String getDepthFragmentShader() {
        return "#version 300 es\n"
                + "precision mediump float;\n"
                + "uniform sampler2D inputImageTexture0;\n"
                + "in vec2 textureCoordinate;\n"
                + "out vec4 fragColor;\n"
                + "\n"
                + "const highp float kMaxDepth = 8000.0; // In millimeters.\n"
                + "\n"
                + "float depthGetMillimeters(in sampler2D depthTexture, in vec2 depthUv) {\n"
                + "    vec3 packedDepthAndVisibility = texture(depthTexture, depthUv).xyz;\n"
                + "    return dot(packedDepthAndVisibility.xy, vec2(255.0, 256.0 * 255.0));\n"
                + "}\n"
                + "\n"
                + "vec3 depthGetColorVisualization(in float x) {\n"
                + "    const vec4 kRedVec4 = vec4(0.55305649, 3.00913185, -5.46192616, -11.11819092);\n"
                + "    const vec4 kGreenVec4 = vec4(0.16207513, 0.17712472, 15.24091500, -36.50657960);\n"
                + "    const vec4 kBlueVec4 = vec4(-0.05195877, 5.18000081, -30.94853351, 81.96403246);\n"
                + "    const vec2 kRedVec2 = vec2(27.81927491, -14.87899417);\n"
                + "    const vec2 kGreenVec2 = vec2(25.95549545, -5.02738237);\n"
                + "    const vec2 kBlueVec2 = vec2(-86.53476570, 30.23299484);\n"
                + "    const float kInvalidDepthThreshold = 0.01;\n"
                + "\n"
                + "    // Adjusts color space via 6 degree poly interpolation to avoid pure red.\n"
                + "    x = clamp(x * 0.9 + 0.03, 0.0, 1.0);\n"
                + "    vec4 v4 = vec4(1.0, x, x * x, x * x * x);\n"
                + "    vec2 v2 = v4.zw * v4.z;\n"
                + "    vec3 polynomial_color = vec3(\n"
                + "    dot(v4, kRedVec4) + dot(v2, kRedVec2),\n"
                + "    dot(v4, kGreenVec4) + dot(v2, kGreenVec2),\n"
                + "    dot(v4, kBlueVec4) + dot(v2, kBlueVec2)\n"
                + "    );\n"
                + "    return step(kInvalidDepthThreshold, x) * polynomial_color;\n"
                + "}\n"
                + "\n"
                + "void main() {\n"
                + "    highp float normalizedDepth =\n"
                + "        clamp(depthGetMillimeters(inputImageTexture0, textureCoordinate.xy) / kMaxDepth, 0.0, 1.0);\n"
                + "    vec4 depthColor = vec4(depthGetColorVisualization(normalizedDepth), 1.0);\n"
                + "    fragColor = depthColor;\n"
                + "}\n";

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
