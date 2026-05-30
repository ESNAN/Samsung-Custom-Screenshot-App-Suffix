package com.suffixfix.xposed;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final class Row {
        String pkg, name, suffix;
        Drawable icon;
    }

    private final List<Row> rows = new ArrayList<>();
    private RowAdapter adapter;
    private TextView title;

    private static final ViewOutlineProvider ICON_CLIP = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                    view.getResources().getDisplayMetrics().density * 12f);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = findViewById(R.id.title);
        ListView list = findViewById(R.id.list);
        adapter = new RowAdapter();
        list.setAdapter(adapter);

        ((Button) findViewById(R.id.btnRefresh)).setOnClickListener(v -> scan());

        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, EditActivity.class);
            i.putExtra("pkg", rows.get(position).pkg);
            startActivity(i);
        });
    }

    private void scan() {
        rows.clear();
        int set = 0;
        SharedPreferences sp = prefs();
        PackageManager pm = getPackageManager();
        for (ApplicationInfo ai : pm.getInstalledApplications(0)) {
            if (pm.getLaunchIntentForPackage(ai.packageName) == null) continue;
            if (!SamsungLabel.cleaned(SamsungLabel.englishLabel(this, ai.packageName)).isEmpty()) continue;

            Row r = new Row();
            r.pkg = ai.packageName;
            r.name = SamsungLabel.displayLabel(this, ai);
            r.suffix = sp.getString(ai.packageName, null);
            if (r.suffix != null && !r.suffix.isEmpty()) set++;
            try {
                r.icon = pm.getApplicationIcon(ai);
            } catch (Exception ignored) {
            }
            rows.add(r);
        }
        adapter.notifyDataSetChanged();
        showCount(set, rows.size() - set);
    }

    private void showCount(int set, int unset) {
        SpannableStringBuilder s = new SpannableStringBuilder();
        s.append(String.valueOf(set));
        s.setSpan(new ForegroundColorSpan(getColor(R.color.accent_green)), 0, s.length(), 0);
        int slash = s.length();
        s.append("/").append(String.valueOf(unset));
        s.setSpan(new ForegroundColorSpan(getColor(R.color.accent_red)), slash + 1, s.length(), 0);
        title.setText(s);
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(Const.PREF_SUFFIX, MODE_PRIVATE);
    }

    private final class RowAdapter extends BaseAdapter {
        @Override public int getCount() { return rows.size(); }
        @Override public Object getItem(int p) { return rows.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView != null ? convertView
                    : getLayoutInflater().inflate(R.layout.list_item, parent, false);
            Row r = rows.get(position);
            ImageView icon = v.findViewById(R.id.icon);
            icon.setOutlineProvider(ICON_CLIP);
            icon.setClipToOutline(true);
            icon.setImageDrawable(r.icon);
            ((TextView) v.findViewById(R.id.appName)).setText(r.name);
            ((TextView) v.findViewById(R.id.pkgName)).setText(r.pkg);

            boolean set = r.suffix != null && !r.suffix.isEmpty();
            v.findViewById(R.id.statusBar).setBackgroundResource(
                    set ? R.drawable.bar_green : R.drawable.bar_red);
            ((TextView) v.findViewById(R.id.suffix)).setText(set ? r.suffix : "");
            return v;
        }
    }
}
