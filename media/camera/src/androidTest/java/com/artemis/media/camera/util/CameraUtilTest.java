package com.artemis.media.camera.util;

import android.util.Size;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.artemis.media.camera.config.CameraConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xrealm on 2020/6/27.
 */
@RunWith(AndroidJUnit4.class)
public class CameraUtilTest {

    private List<Size> outputSizes = new ArrayList<>();

    @Before
    public void setUp() {
        outputSizes.add(new Size(2816, 2112));
        outputSizes.add(new Size(2816, 1584));
        outputSizes.add(new Size(2560, 1080));
        outputSizes.add(new Size(2160, 1080));
        outputSizes.add(new Size(1920, 1080));
        outputSizes.add(new Size(1920, 960));
        outputSizes.add(new Size(1440, 1080));
        outputSizes.add(new Size(1680, 720));
        outputSizes.add(new Size(1440, 720));
        outputSizes.add(new Size(1280, 960));
        outputSizes.add(new Size(1280, 720));
        outputSizes.add(new Size(960, 720));
        outputSizes.add(new Size(720, 720));
        outputSizes.add(new Size(640, 480));
        outputSizes.add(new Size(320, 240));
        outputSizes.add(new Size(352, 288));
        outputSizes.add(new Size(208, 144));
        outputSizes.add(new Size(176, 144));
    }

    @Test
    public void chooseSizeTest() {
        Size targetSize = new Size(1280, 720);
        CameraConfig config = CameraConfig.obtain();
        Size result = Camera2Util.chooseOptimalSize(outputSizes, targetSize, config);
        System.out.println("test " + result);
    }
}
