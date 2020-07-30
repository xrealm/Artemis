package com.artemis.cv;

public interface ILicenseListener {

    void onSuccess();

    void onFailed(int what, String msg);
}
