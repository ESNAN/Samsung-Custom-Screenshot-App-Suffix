package com.suffixfix.xposed;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

public class EditActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        String pkg = getIntent().getStringExtra("pkg");
        Map<String, String> map = Config.load();

        ((TextView) findViewById(R.id.appName)).setText(
                getPackageManager().getApplicationLabel(getAppInfo(pkg)).toString());
        ((TextView) findViewById(R.id.pkgName)).setText(pkg);

        EditText input = findViewById(R.id.input);
        String cur = map.get(pkg);
        input.setText(cur == null ? "" : cur);

        ((Button) findViewById(R.id.btnSave)).setOnClickListener(v -> {
            String val = input.getText().toString().trim();
            if (val.isEmpty()) map.remove(pkg); else map.put(pkg, val);
            Config.save(map);
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
}
