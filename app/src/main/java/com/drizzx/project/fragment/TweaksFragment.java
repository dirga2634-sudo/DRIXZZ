package com.drizzx.project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.drizzx.project.R;
import com.drizzx.project.ShizukuHelper;
import com.drizzx.project.databinding.FragmentTweaksBinding;

public class TweaksFragment extends Fragment {

    private FragmentTweaksBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTweaksBinding.inflate(inflater, container, false);

        setupTweaks();
        return binding.getRoot();
    }

    private void setupTweaks() {

        // Mouse Config
        binding.switchMouse.setOnCheckedChangeListener((btn, checked) -> {
            if (!check()) { btn.setChecked(false); return; }
            ShizukuHelper.run(checked ?
                "settings put system show_touches 1" :
                "settings put system show_touches 0",
                (ok, out) -> requireActivity().runOnUiThread(() ->
                    toast(checked ? "Mouse config aktif" : "Mouse config nonaktif")));
        });

        // Sensitivity
        binding.switchSensitivity.setOnCheckedChangeListener((btn, checked) -> {
            if (!check()) { btn.setChecked(false); return; }
            binding.layoutSensitivity.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        for (int i = 1; i <= 7; i++) {
            int level = i;
            int id = getResources().getIdentifier("btn_sens_" + i, "id", requireContext().getPackageName());
            View btn = binding.getRoot().findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    if (!check()) return;
                    ShizukuHelper.setSensitivity(level - 4, (ok, out) ->
                        requireActivity().runOnUiThread(() ->
                            toast("Sensitivity level " + level + " diterapkan")));
                });
            }
        }

        // SurfaceFlinger
        binding.switchSurfaceflinger.setOnCheckedChangeListener((btn, checked) -> {
            if (!check()) { btn.setChecked(false); return; }
            ShizukuHelper.setSurfaceFlinger(checked, (ok, out) ->
                requireActivity().runOnUiThread(() ->
                    toast("SurfaceFlinger " + (checked ? "aktif" : "nonaktif"))));
        });

        // GPU Acceleration
        binding.switchGpu.setOnCheckedChangeListener((btn, checked) -> {
            if (!check()) { btn.setChecked(false); return; }
            ShizukuHelper.run(checked ?
                "settings put global gpu_debug_layers_gles ''" :
                "settings put global gpu_debug_layers_gles ''",
                (ok, out) -> requireActivity().runOnUiThread(() ->
                    toast("GPU Acceleration " + (checked ? "aktif" : "nonaktif"))));
        });

        // Clean Cache
        binding.btnCleanCache.setOnClickListener(v -> {
            if (!check()) return;
            ShizukuHelper.cleanCache((ok, out) ->
                requireActivity().runOnUiThread(() ->
                    toast(ok ? "Cache berhasil dibersihkan" : "Gagal: " + out)));
        });
    }

    private boolean check() {
        if (!ShizukuHelper.isRunning()) { toast("Shizuku tidak aktif"); return false; }
        if (!ShizukuHelper.hasPermission()) { ShizukuHelper.requestPermission(); toast("Izinkan Shizuku"); return false; }
        return true;
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
