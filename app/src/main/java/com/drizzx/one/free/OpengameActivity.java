package com.drizzx.one.free;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drizzx.one.free.databinding.ActivityOpengameBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class OpengameActivity extends AppCompatActivity {

    private ActivityOpengameBinding binding;
    private GameAdapter adapter;
    private List<AppInfo> allGames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOpengameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Open Game");
        }

        adapter = new GameAdapter(new ArrayList<>(), this::onAppClick);
        binding.rvGames.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvGames.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterApps(s.toString());
            }
        });

        loadApps();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    private void loadApps() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvGames.setVisibility(View.GONE);

        new Thread(() -> {
            PackageManager pm = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<AppInfo> apps = new ArrayList<>();
            List<android.content.pm.ResolveInfo> list = pm.queryIntentActivities(intent, 0);

            for (android.content.pm.ResolveInfo ri : list) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(ri.activityInfo.packageName, 0);
                    if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
                    if (ai.packageName.equals(getPackageName())) continue;

                    AppInfo info = new AppInfo();
                    info.name = pm.getApplicationLabel(ai).toString();
                    info.packageName = ai.packageName;
                    info.icon = pm.getApplicationIcon(ai);
                    apps.add(info);
                } catch (Exception ignored) {}
            }

            apps.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
            allGames = apps;

            runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.rvGames.setVisibility(View.VISIBLE);
                adapter.updateData(apps);
                binding.tvCount.setText(apps.size() + " apps ditemukan");
            });
        }).start();
    }

    private void filterApps(String q) {
        if (allGames == null) return;
        List<AppInfo> filtered = new ArrayList<>();
        for (AppInfo a : allGames) {
            if (a.name.toLowerCase().contains(q.toLowerCase()) ||
                    a.packageName.toLowerCase().contains(q.toLowerCase())) {
                filtered.add(a);
            }
        }
        adapter.updateData(filtered);
        binding.tvCount.setText(filtered.size() + " app ditemukan");
    }

    private void onAppClick(AppInfo app) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(app.name)
                .setMessage("Package: " + app.packageName)
                .setNegativeButton("Batal", null)
                .setNeutralButton("Force Stop", (d, w) -> {
                    if (ShizukuHelper.hasPermission()) {
                        ShizukuHelper.forceStop(app.packageName, (ok, out) ->
                                runOnUiThread(() -> Toast.makeText(this,
                                        ok ? "✅ Stopped" : "❌ " + out, Toast.LENGTH_SHORT).show()));
                    } else showToast("Butuh Shizuku");
                })
                .setPositiveButton("▶ Buka", (d, w) -> {
                    Intent launch = getPackageManager().getLaunchIntentForPackage(app.packageName);
                    if (launch != null) startActivity(launch);
                    else showToast("Tidak bisa dibuka");
                }).show();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // Data model
    static class AppInfo {
        String name, packageName;
        Drawable icon;
    }

    // Adapter
    static class GameAdapter extends RecyclerView.Adapter<GameAdapter.VH> {
        private List<AppInfo> data;
        private final OnClick listener;
        interface OnClick { void onClick(AppInfo a); }

        GameAdapter(List<AppInfo> data, OnClick l) { this.data = data; this.listener = l; }

        void updateData(List<AppInfo> d) { this.data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_game, p, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            AppInfo a = data.get(pos);
            h.icon.setImageDrawable(a.icon);
            h.name.setText(a.name);
            h.itemView.setOnClickListener(v -> listener.onClick(a));
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            ImageView icon; TextView name;
            VH(View v) { super(v); icon = v.findViewById(R.id.iv_app_icon); name = v.findViewById(R.id.tv_app_name); }
        }
    }
}
