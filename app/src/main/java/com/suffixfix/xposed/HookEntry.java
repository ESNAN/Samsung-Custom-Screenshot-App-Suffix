package com.suffixfix.xposed;

import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        if (!"com.android.systemui".equals(lpparam.packageName)) return;

        final ClassLoader cl = lpparam.classLoader;
        Class<?> exporter = XposedHelpers.findClassIfExists(
                "com.android.systemui.screenshot.ImageExporter", cl);
        if (exporter == null) return;
        XposedBridge.log("SuffixFix: loaded, ImageExporter found in " + lpparam.processName);

        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String name = (String) XposedHelpers.getStaticObjectField(exporter, "mImageFileName");
                String patched = patch(cl, name);
                if (patched == null) return;
                XposedHelpers.setStaticObjectField(exporter, "mImageFileName", patched);
                Object dn = XposedHelpers.getStaticObjectField(exporter, "mImageDisplayName");
                if (dn != null) {
                    XposedHelpers.setStaticObjectField(exporter, "mImageDisplayName", stripExt(patched));
                }
                Object fp = XposedHelpers.getStaticObjectField(exporter, "mImageFilePath");
                if (fp instanceof String) {
                    String p = (String) fp;
                    int slash = p.lastIndexOf('/');
                    if (slash >= 0) {
                        XposedHelpers.setStaticObjectField(exporter, "mImageFilePath",
                                p.substring(0, slash + 1) + patched);
                    }
                }
                XposedBridge.log("SuffixFix: " + name + " -> " + patched);
            }
        };
        XposedHelpers.findAndHookMethod(exporter, "semCreateMetadata", hook);

        Class<?> proxy = XposedHelpers.findClassIfExists(
                "com.samsung.android.app.smartcapture.screenshot.lib.IScreenshotService$Stub$Proxy", cl);
        if (proxy == null) {
            proxy = XposedHelpers.findClassIfExists(
                    "com.android.systemui.screenshot.lib.IScreenshotService$Stub$Proxy", cl);
        }
        if (proxy != null) {
            for (java.lang.reflect.Method m : proxy.getDeclaredMethods()) {
                String mn = m.getName();
                if (!mn.equals("onGlobalScreenshotStarted") && !mn.equals("onGlobalScreenshotFinished")) continue;
                XposedBridge.log("SuffixFix: hooking " + m);
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        for (int i = 0; i < param.args.length; i++) {
                            if (!(param.args[i] instanceof String)) continue;
                            String p = (String) param.args[i];
                            int slash = p.lastIndexOf('/');
                            String dir = slash >= 0 ? p.substring(0, slash + 1) : "";
                            String fn = slash >= 0 ? p.substring(slash + 1) : p;
                            String patched = patch(cl, fn);
                            if (patched != null) {
                                param.args[i] = dir + patched;
                                XposedBridge.log("SuffixFix: remote path " + p + " -> " + param.args[i]);
                            }
                        }
                    }
                });
            }
        } else {
            XposedBridge.log("SuffixFix: IScreenshotService$Stub$Proxy NOT found");
        }
    }

    private static String patch(ClassLoader cl, String name) {
        if (name == null) return null;
        int dot = name.lastIndexOf('.');
        String base = dot >= 0 ? name.substring(0, dot) : name;
        String ext = dot >= 0 ? name.substring(dot) : "";
        if (!base.matches("Screenshot_\\d+_\\d+")) return null;

        Context ctx = currentApplication();
        if (ctx == null) return null;
        String pkg = topPackage(cl, ctx);
        if (pkg == null) return null;
        String suffix = loadSuffix(pkg);
        if (suffix == null || suffix.isEmpty()) return null;
        return base + "_" + suffix + ext;
    }

    private static String stripExt(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(0, dot) : name;
    }

    private static Context currentApplication() {
        try {
            Class<?> at = Class.forName("android.app.ActivityThread");
            return (Context) XposedHelpers.callStaticMethod(at, "currentApplication");
        } catch (Throwable t) {
            return null;
        }
    }

    private static String topPackage(ClassLoader cl, Context ctx) {
        try {
            Class<?> u = XposedHelpers.findClass(
                    "com.android.systemui.screenshot.sep.ScreenshotUtils", cl);
            return (String) XposedHelpers.callStaticMethod(u, "getTopMostApplicationPackage", ctx);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String loadSuffix(String pkg) {
        return Config.get(pkg);
    }
}
