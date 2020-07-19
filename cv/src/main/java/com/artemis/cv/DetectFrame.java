package com.artemis.cv;

public class DetectFrame {

    public static final int FMT_NV21 = 1;
    public static final int FMT_RGBA = 2;

    public int width;
    public int height;
    public byte[] data;
    public int format;
}
