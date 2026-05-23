package com.drizzx.project.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drizzx.project.PrefManager;
import com.drizzx.project.R;
import com.drizzx.project.ShizukuHelper;
import com.drizzx.project.databinding.FragmentLauncherBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class LauncherFragment extends Fragment {

    private FragmentLauncherBinding binding;
    private AppAdapter adapter;
    private List<AppInfo> allApps = new ArrayList<>();
    private PrefManager pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLauncherBinding.inflate(inflater, container, false);
        pref = new PrefManager(requireContext());

        adapter = new AppAdapter(new ArrayList<>(), this::onAppClick);
        binding.rvApps.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvApps.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filter(s.toString());
            }
        });

        loadApps();
        return binding.getRoot();
    }

    private void loadApps() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvApps.setVisibility(View.GONE);

        new Thread(() -> {
            PackageManager pm = requireContext().getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<AppInfo> apps = new ArrayList<>();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);

            for (ResolveInfo ri : list) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(ri.activityInfo.packageName, 0);
                    if (ai.packageName.equals(requireContext().getPackageName())) continue;

                    AppInfo info = new AppInfo();
                    info.name = pm.getApplicationLabel(ai).toString();
                    info.packageName = ai.packageName;
                    info.activityName = ri.activityInfo.name;
                    info.icon = pm.getApplicationIcon(ai);
                    apps.add(info);
                } catch (Exception ignored) {}
            }

            apps.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
            allApps = apps;

            requireActivity().runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.rvApps.setVisibility(View.VISIBLE);
                adapter.setData(apps);
                binding.tvCount.setText(apps.size() + " aplikasi");
            });
        }).start();
    }

    private void filter(String q) {
        if (allApps == null) return;
        List<AppInfo> filtered = new ArrayList<>();
        String ql = q.toLowerCase();
        for (AppInfo a : allApps) {
            if (a.name.toLowerCase().contains(ql) || a.packageName.toLowerCase().contains(ql)) {
                filtered.add(a);
            }
        }
        adapter.setData(filtered);
        binding.tvCount.setText(filtered.size() + " aplikasi");
    }

    private void onAppClick(AppInfo app) {
        String savedMode = pref.getPerfMode();
        int savedIndex = getModeIndex(savedMode);
        String[] modes = {"Normal", "Mode Performa", "Mode Hemat Baterai", "Force Stop + Buka"};
        final int[] selected = {savedIndex};

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(app.name)
            .setMessage("Package: " + app.packageName)
            .setSingleChoiceItems(modes, savedIndex, (d, which) -> selected[0] = which)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Buka", (d, w) -> {
                String modeKey = getModeKey(selected[0]);
                pref.setPerfMode(modeKey);
                launchWithMode(app, modeKey);
            })
            .show();
    }

    private void launchWithMode(AppInfo app, String mode) {
        switch (mode) {
            case "performance":
                Toast.makeText(requireContext(), "Mode Performa aktif...", Toast.LENGTH_SHORT).show();
                if (ShizukuHelper.hasPermission()) {
                    ShizukuHelper.run(
                        "for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; " +
                        "do echo performance > $cpu 2>/dev/null; done; " +
                        "settings put global animator_duration_scale 0",
                        (ok, out) -> requireActivity().runOnUiThread(() -> doLaunch(app)));
                } else {
                    doLaunch(app);
                }
                break;

            case "powersave":
                Toast.makeText(requireContext(), "Mode Hemat Baterai aktif...", Toast.LENGTH_SHORT).show();
                if (ShizukuHelper.hasPermission()) {
                    ShizukuHelper.run(
                        "for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; " +
                        "do echo powersave > $cpu 2>/dev/null; done",
                        (ok, out) -> requireActivity().runOnUiThread(() -> doLaunch(app)));
                } else {
                    doLaunch(app);
                }
                break;

            case "forcestop":
                Toast.makeText(requireContext(), "Force stop dulu...", Toast.LENGTH_SHORT).show();
                if (ShizukuHelper.hasPermission()) {
                    ShizukuHelper.forceStop(app.packageName, (ok, out) ->
                        requireActivity().runOnUiThread(() -> {
                            try { Thread.sleep(800); } catch (Exception ignored) {}
                            doLaunch(app);
                        }));
                } else {
                    doLaunch(app);
                }
                break;

            default:
                doLaunch(app);
                break;
        }
    }

    private void doLaunch(AppInfo app) {
        try {
            // Cara 1: pakai ComponentName langsung (paling reliable)
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(app.packageName, app.activityName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
        } catch (Exception e1) {
            try {
                // Cara 2: pakai getLaunchIntentForPackage
                PackageManager pm = requireContext().getPackageManager();
                Intent launch = pm.getLaunchIntentForPackage(app.packageName);
                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launch);
                } else {
                    Toast.makeText(requireContext(), "Tidak bisa membuka " + app.name, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e2) {
                Toast.makeText(requireContext(), "Error: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getModeKey(int index) {
        switch (index) {
            case 1: return "performance";
            case 2: return "powersave";
            case 3: return "forcestop";
            default: return "normal";
        }
    }

    private int getModeIndex(String key) {
        switch (key) {
            case "performance": return 1;
            case "powersave": return 2;
            case "forcestop": return 3;
            default: return 0;
        }
    }

    static class AppInfo {
        String name, packageName, activityName;
        Drawable icon;
    }

    static class AppAdapter extends RecyclerView.Adapter<AppAdapter.VH> {
        private List<AppInfo> data;
        private final OnClick listener;
        interface OnClick { void onClick(AppInfo a); }

        AppAdapter(List<AppInfo> data, OnClick l) { this.data = data; this.listener = l; }

        void setData(List<AppInfo> d) { this.data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_app, p, false);
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
            VH(View v) {
                super(v);
                icon = v.findViewById(R.id.iv_icon);
                name = v.findViewById(R.id.tv_name);
            }
        }
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
