package com.suffixfix.xposed;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Activity {

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        String pkg = getIntent().getStringExtra("pkg");
        SharedPreferences sp = prefs();

        ((TextView) findViewById(R.id.appName)).setText(
                getPackageManager().getApplicationLabel(getAppInfo(pkg)).toString());
        ((TextView) findViewById(R.id.pkgName)).setText(pkg);

        EditText input = findViewById(R.id.input);
        input.setText(sp.getString(pkg, ""));

        ((Button) findViewById(R.id.btnSave)).setOnClickListener(v -> {
            String val = input.getText().toString().trim();
            SharedPreferences.Editor e = sp.edit();
            if (val.isEmpty()) e.remove(pkg); else e.putString(pkg, val);
            e.apply();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private android.content.pm.ApplicationInfo getAppInfo(String pkg) {
        try {
            return getPackageManager().getApplicationInfo(pkg, 0);
        } catch (Exception e) {
            android.content.pm.ApplicationInfo ai = new android.content.pm.ApplicationInfo();
            ai.packageName = pkg;
            return ai;
        }
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(Const.PREF_SUFFIX, MODE_PRIVATE);
    }
}
