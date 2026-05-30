package com.suffixfix.xposed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public final class Config {

    public static Map<String, String> load() {
        Map<String, String> map = new TreeMap<>();
        File f = new File(Const.CONFIG_PATH);
        if (!f.exists()) return map;
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(f)) {
            p.load(in);
        } catch (Exception ignored) {
            return map;
        }
        for (String k : p.stringPropertyNames()) {
            String v = p.getProperty(k);
            if (v != null && !v.isEmpty()) map.put(k, v);
        }
        return map;
    }

    public static String get(String pkg) {
        return load().get(pkg);
    }

    public static void save(Map<String, String> map) {
        File f = new File(Const.CONFIG_PATH);
        File dir = f.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        Properties p = new Properties();
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) p.setProperty(e.getKey(), e.getValue());
        }
        try (FileOutputStream out = new FileOutputStream(f)) {
            p.store(out, "SuffixFix");
        } catch (Exception ignored) {
        }
    }
}
