package com.artemis.cv;

public interface ILicenseListener {

    void onSucess();

    void onFailed(int what, String msg);
}
