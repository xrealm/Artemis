package com.artemis.media.camera.util;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.util.Range;
import android.util.Size;

import com.artemis.media.camera.config.CameraConfig;
import com.artemis.media.camera.log.CamLog;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by xrealm on 2020/6/26.
 */
public class Camera2Util {

    private static final String TAG = "[CameraUtils.java]";

    public static void chooseOptimalFpsRange(CameraCharacteristics characteristics, CameraConfig config) {
        List<Range<Integer>> fpsRanges = Arrays.asList(characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES));
        final int targetFps = config.getVideoFps();
        Collections.sort(fpsRanges, new Comparator<Range<Integer>>() {
            @Override
            public int compare(Range<Integer> lhs, Range<Integer> rhs) {
                int r = Math.abs(lhs.getLower() - targetFps) + Math.abs(lhs.getUpper() - targetFps);
                int l = Math.abs(rhs.getLower() - targetFps) + Math.abs(rhs.getUpper() - targetFps);
                if (r > l) {
                    return 1;
                } else if (r < l) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        config.previewMinFps = fpsRanges.get(0).getLower();
        config.previewMaxFps = fpsRanges.get(0).getUpper();
        CamLog.i(TAG, "choose fps Range: " + config.previewMinFps + "-" + config.previewMaxFps);
    }

    public static Size chooseOptimalPreviewSize(CameraCharacteristics characteristics, CameraConfig config) {
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        return chooseOptimalSize(Arrays.asList(map.getOutputSizes(SurfaceTexture.class)), config.getPreviewSize(), config);
    }

    public static Size chooseOptimalSize(List<Size> outputSizes, Size targetSize, CameraConfig config) {
        Collections.sort(outputSizes, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                if ((lhs.getWidth() * lhs.getHeight()) > (rhs.getWidth() * rhs.getHeight())) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        int previewVideoWidth = 0;
        int previewVideoHeight = 0;
        for (Size size : outputSizes) {
            if (size.getWidth() >= targetSize.getWidth() && size.getHeight() >= targetSize.getHeight()) {
                previewVideoWidth = size.getWidth();
                previewVideoHeight = size.getHeight();
                break;
            }
        }
        Size size = new Size(previewVideoWidth, previewVideoHeight);
        CamLog.i(TAG, "chooseOptimalSize: Size: " + size);
        config.previewVideoWidth = previewVideoWidth;
        config.previewVideoHeight = previewVideoHeight;
        config.setPreviewSize(size);
        return size;
    }

    public static byte[] yuv420Data(Image image) {
        final int imageWidth = image.getWidth();
        final int imageHeight = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[imageWidth * imageHeight *
                ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
        int offset = 0;

        for (int plane = 0; plane < planes.length; ++plane) {
            final ByteBuffer buffer = planes[plane].getBuffer();
            final int rowStride = planes[plane].getRowStride();
            // Experimentally, U and V planes have |pixelStride| = 2, which
            // essentially means they are packed.
            final int pixelStride = planes[plane].getPixelStride();
            final int planeWidth = (plane == 0) ? imageWidth : imageWidth / 2;
            final int planeHeight = (plane == 0) ? imageHeight : imageHeight / 2;
            if (pixelStride == 1 && rowStride == planeWidth) {
                // Copy whole plane from buffer into |data| at once.
                buffer.get(data, offset, planeWidth * planeHeight);
                offset += planeWidth * planeHeight;
            } else {
                // Copy pixels one by one respecting pixelStride and rowStride.
                byte[] rowData = new byte[rowStride];
                for (int row = 0; row < planeHeight - 1; ++row) {
                    buffer.get(rowData, 0, rowStride);
                    for (int col = 0; col < planeWidth; ++col) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
                // Last row is special in some devices and may not contain the full
                // |rowStride| bytes of data.
                // See http://developer.android.com/reference/android/media/Image.Plane.html#getBuffer()
                buffer.get(rowData, 0, Math.min(rowStride, buffer.remaining()));
                for (int col = 0; col < planeWidth; ++col) {
                    data[offset++] = rowData[col * pixelStride];
                }
            }
        }

        return data;
    }

    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;
    private static byte[] rowData = null;

    public static byte[] yuv420888toNv21(Image image, byte[] outData) {
        int colorFormat = COLOR_FormatNV21;
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();

        byte[] data = null;
        if (outData == null || outData.length != width * height * ImageFormat.getBitsPerPixel(format) / 8) {
            data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        } else {
            data = outData;
        }
        if (rowData == null || rowData.length != planes[0].getRowStride()) {

            rowData = new byte[planes[0].getRowStride()];
        }

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
//            if (true) {
//                Log.v(TAG, "pixelStride " + pixelStride);
//                Log.v(TAG, "rowStride " + rowStride);
//                Log.v(TAG, "width " + width);
//                Log.v(TAG, "height " + height);
//                Log.v(TAG, "buffer size " + buffer.remaining());
//            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
//            if (true) Log.v(TAG, "Finished reading data from plane " + i);
        }
//        }
        return data;
    }
}
