package com.hydrologis.cn1.libs;

import android.Manifest;

public class HyNativeUtilsImpl {

    public boolean checkPemissions(String param, String param1) {
        if (param.equals(HyNativeUtils.PERMISSION_WRITE_EXTERNAL_STORAGE)) {
            return com.codename1.impl.android.AndroidImplementation.checkForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, param1);
        }
        return false;
    }

    public boolean isSupported() {
        return true;
    }

}
