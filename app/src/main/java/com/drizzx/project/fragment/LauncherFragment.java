package com.drizzx.project.fragment;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLauncherBinding.inflate(inflater, container, false);

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
        new Thread(() -> {
            PackageManager pm = requireContext().getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<AppInfo> apps = new ArrayList<>();
            for (android.content.pm.ResolveInfo ri : pm.queryIntentActivities(intent, 0)) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(ri.activityInfo.packageName, 0);
                    if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
                    if (ai.packageName.equals(requireContext().getPackageName())) continue;
                    AppInfo info = new AppInfo();
                    info.name = pm.getApplicationLabel(ai).toString();
                    info.packageName = ai.packageName;
                    info.icon = pm.getApplicationIcon(ai);
                    apps.add(info);
                } catch (Exception ignored) {}
            }
            apps.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
            allApps = apps;
            requireActivity().runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);
                adapter.setData(apps);
                binding.tvCount.setText(apps.size() + " aplikasi");
            });
        }).start();
    }

    private void filter(String q) {
        List<AppInfo> filtered = new ArrayList<>();
        for (AppInfo a : allApps) {
            if (a.name.toLowerCase().contains(q.toLowerCase()) ||
                a.packageName.toLowerCase().contains(q.toLowerCase())) {
                filtered.add(a);
            }
        }
        adapter.setData(filtered);
        binding.tvCount.setText(filtered.size() + " aplikasi");
    }

    private void onAppClick(AppInfo app) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(app.name)
            .setMessage("Package: " + app.packageName)
            .setNegativeButton("Tutup", null)
            .setNeutralButton("Force Stop", (d, w) -> {
                if (ShizukuHelper.hasPermission()) {
                    ShizukuHelper.forceStop(app.packageName, (ok, out) ->
                        requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), ok ? "Stopped" : "Gagal", Toast.LENGTH_SHORT).show()));
                }
            })
            .setPositiveButton("Buka", (d, w) -> {
                Intent launch = requireContext().getPackageManager().getLaunchIntentForPackage(app.packageName);
                if (launch != null) startActivity(launch);
                else Toast.makeText(requireContext(), "Tidak bisa dibuka", Toast.LENGTH_SHORT).show();
            }).show();
    }

    static class AppInfo {
        String name, packageName;
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
            VH(View v) { super(v); icon = v.findViewById(R.id.iv_icon); name = v.findViewById(R.id.tv_name); }
        }
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
