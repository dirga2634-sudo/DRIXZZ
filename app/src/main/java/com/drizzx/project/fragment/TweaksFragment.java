package com.drizzx.project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.drizzx.project.PrefManager;
import com.drizzx.project.R;
import com.drizzx.project.ShizukuHelper;
import com.drizzx.project.databinding.FragmentTweaksBinding;

public class TweaksFragment extends Fragment {

    private FragmentTweaksBinding binding;
    private PrefManager pref;
    private int selectedSens = 4;
    private final TextView[] sensBtns = new TextView[7];
    private boolean isLoading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTweaksBinding.inflate(inflater, container, false);
        pref = new PrefManager(requireContext());
        selectedSens = pref.getSensLevel();

        sensBtns[0] = binding.btnSens1;
        sensBtns[1] = binding.btnSens2;
        sensBtns[2] = binding.btnSens3;
        sensBtns[3] = binding.btnSens4;
        sensBtns[4] = binding.btnSens5;
        sensBtns[5] = binding.btnSens6;
        sensBtns[6] = binding.btnSens7;

        loadState();
        setupListeners();
        updateSensUI(selectedSens);
        return binding.getRoot();
    }

    private void loadState() {
        isLoading = true;
        binding.switchMouse.setChecked(pref.getMouseConfig());
        binding.switchSensitivity.setChecked(pref.getSensEnabled());
        binding.switchSurfaceflinger.setChecked(pref.getSurfaceFlinger());
        binding.switchGpu.setChecked(pref.getGpu());
        binding.layoutSensitivity.setVisibility(pref.getSensEnabled() ? View.VISIBLE : View.GONE);
        isLoading = false;
    }

    private void setupListeners() {
        // Sensitivity buttons
        for (int i = 0; i < 7; i++) {
            final int level = i + 1;
            sensBtns[i].setOnClickListener(v -> {
                selectedSens = level;
                pref.setSensLevel(level);
                updateSensUI(level);
                if (!checkShizuku()) return;
                ShizukuHelper.setSensitivity(level - 4, (ok, out) ->
                    requireActivity().runOnUiThread(() ->
                        toast("Sensitivity level " + level + " diterapkan")));
            });
        }

        binding.switchMouse.setOnCheckedChangeListener((btn, checked) -> {
            if (isLoading) return;
            pref.setMouseConfig(checked);
            if (!checkShizuku()) return;
            ShizukuHelper.run(checked ?
                "settings put system show_touches 1" :
                "settings put system show_touches 0",
                (ok, out) -> requireActivity().runOnUiThread(() ->
                    toast("Mouse config " + (checked ? "aktif" : "nonaktif"))));
        });

        binding.switchSensitivity.setOnCheckedChangeListener((btn, checked) -> {
            if (isLoading) return;
            pref.setSensEnabled(checked);
            binding.layoutSensitivity.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        binding.switchSurfaceflinger.setOnCheckedChangeListener((btn, checked) -> {
            if (isLoading) return;
            pref.setSurfaceFlinger(checked);
            if (!checkShizuku()) return;
            ShizukuHelper.setSurfaceFlinger(checked, (ok, out) ->
                requireActivity().runOnUiThread(() ->
                    toast("SurfaceFlinger " + (checked ? "aktif" : "nonaktif"))));
        });

        binding.switchGpu.setOnCheckedChangeListener((btn, checked) -> {
            if (isLoading) return;
            pref.setGpu(checked);
            if (!checkShizuku()) return;
            String cmd = checked ?
                "settings put global hardware_renderer_disabled 0" :
                "settings put global hardware_renderer_disabled 1";
            ShizukuHelper.run(cmd, (ok, out) ->
                requireActivity().runOnUiThread(() ->
                    toast("GPU Acceleration " + (checked ? "aktif" : "nonaktif"))));
        });

        binding.btnCleanCache.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            toast("Membersihkan cache...");
            ShizukuHelper.cleanCache((ok, out) ->
                requireActivity().runOnUiThread(() ->
                    toast(ok ? "Cache berhasil dibersihkan" : "Gagal: " + out)));
        });
    }

    private void updateSensUI(int selected) {
        for (int i = 0; i < 7; i++) {
            if (sensBtns[i] == null) continue;
            if (i + 1 == selected) {
                sensBtns[i].setBackgroundResource(R.drawable.bg_sens_selected);
                sensBtns[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.cyan));
            } else {
                sensBtns[i].setBackgroundResource(R.drawable.bg_btn_outline);
                sensBtns[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dim));
            }
        }
    }

    private boolean checkShizuku() {
        if (!ShizukuHelper.isRunning()) { toast("Shizuku tidak aktif"); return false; }
        if (!ShizukuHelper.hasPermission()) {
            ShizukuHelper.requestPermission();
            toast("Izinkan Shizuku terlebih dahulu");
            return false;
        }
        return true;
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
