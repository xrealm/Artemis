package com.artemis.cv;

public class FrameInfo {
    public SingleFaceInfo[] faceInfos;

    public boolean isFrontCamera;
    public int width;
    public int height;
    public byte[] frameData;

    public int getFaceCnt() {
        return faceInfos != null ? faceInfos.length : 0;
    }
}
