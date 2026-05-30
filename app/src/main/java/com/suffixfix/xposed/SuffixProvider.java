package com.suffixfix.xposed;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.Map;

public class SuffixProvider extends ContentProvider {

    public static final String AUTHORITY = "com.suffixfix.xposed.provider";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/suffix");

    private SharedPreferences sp;

    @Override
    public boolean onCreate() {
        sp = getContext().getSharedPreferences(Const.PREF_SUFFIX, 0);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] p, String s, String[] a, String o) {
        MatrixCursor c = new MatrixCursor(new String[]{"pkg", "suffix"});
        for (Map.Entry<String, ?> e : sp.getAll().entrySet()) {
            Object v = e.getValue();
            if (v instanceof String) c.addRow(new Object[]{e.getKey(), v});
        }
        return c;
    }

    @Override public String getType(Uri uri) { return null; }
    @Override public Uri insert(Uri uri, ContentValues v) { return null; }
    @Override public int delete(Uri uri, String s, String[] a) { return 0; }
    @Override public int update(Uri uri, ContentValues v, String s, String[] a) { return 0; }
}
