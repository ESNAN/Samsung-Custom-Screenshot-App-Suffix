package com.suffixfix.xposed;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public final class SamsungLabel {

    static String englishLabel(Context ctx, String pkg) {
        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA);
            Configuration cfg = new Configuration();
            cfg.setLocale(new Locale("en"));
            Resources res = pm.getResourcesForApplication(ai, cfg);
            return res.getString(ai.labelRes);
        } catch (Exception e) {
            return "";
        }
    }

    static String cleaned(String label) {
        if (label == null) return "";
        return label.replaceAll("[^\\p{ASCII}]", "")
                .replaceAll(System.getProperty("line.separator"), " ")
                .replaceAll("[\\\\/?%*:|\"<>.]", "");
    }

    static String displayLabel(Context ctx, ApplicationInfo ai) {
        try {
            return ctx.getPackageManager().getApplicationLabel(ai).toString();
        } catch (Exception e) {
            return ai.packageName;
        }
    }
}
