package com.suffixfix.xposed;

public final class Const {
    public static final String PKG = "com.suffixfix.xposed";
    public static final String PREF_SUFFIX = "suffix_map";
    public static final String TARGET_PKG = "com.samsung.android.app.smartcapture";
    public static final String TARGET_CLASS =
            "com.samsung.android.app.smartcapture.baseutil.device.DeviceUtils";
    public static final String TARGET_METHOD = "getTopMostApplicationName";
}
