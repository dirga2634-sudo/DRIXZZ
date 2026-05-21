package com.drizzx.project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.drizzx.project.ShizukuHelper;
import com.drizzx.project.databinding.FragmentSensiupBinding;

public class SensiUpFragment extends Fragment {

    private FragmentSensiupBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSensiupBinding.inflate(inflater, container, false);
        setupResolution();
        return binding.getRoot();
    }

    private void setupResolution() {
        // Set current resolution info
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        binding.tvCurrentRes.setText(dm.widthPixels + " x " + dm.heightPixels);
        binding.etWidth.setText(String.valueOf(dm.widthPixels));
        binding.etHeight.setText(String.valueOf(dm.heightPixels));
        binding.etDpi.setText(String.valueOf(dm.densityDpi));

        // Apply Resolution
        binding.btnApplyRes.setOnClickListener(v -> {
            if (!check()) return;
            String w = binding.etWidth.getText().toString().trim();
            String h = binding.etHeight.getText().toString().trim();
            String dpi = binding.etDpi.getText().toString().trim();
            if (w.isEmpty() || h.isEmpty() || dpi.isEmpty()) {
                toast("Isi semua field");
                return;
            }
            ShizukuHelper.setResolution(Integer.parseInt(w), Integer.parseInt(h), (ok, out) ->
                requireActivity().runOnUiThread(() -> toast(ok ? "Resolusi diterapkan: " + w + "x" + h : "Gagal: " + out)));
            ShizukuHelper.setDpi(Integer.parseInt(dpi), (ok, out) ->
                requireActivity().runOnUiThread(() -> {
                    if (ok) toast("DPI diterapkan: " + dpi);
                }));
        });

        // Reset
        binding.btnResetRes.setOnClickListener(v -> {
            if (!check()) return;
            ShizukuHelper.resetResolution((ok, out) -> {});
            ShizukuHelper.resetDpi((ok, out) ->
                requireActivity().runOnUiThread(() -> toast(ok ? "Resolusi & DPI direset" : "Gagal: " + out)));
        });

        // Preset buttons
        binding.btnPreset720.setOnClickListener(v -> setPreset(720, 1600, 230));
        binding.btnPreset1080.setOnClickListener(v -> setPreset(1080, 2400, 395));
        binding.btnPreset540.setOnClickListener(v -> setPreset(540, 1200, 160));
    }

    private void setPreset(int w, int h, int dpi) {
        binding.etWidth.setText(String.valueOf(w));
        binding.etHeight.setText(String.valueOf(h));
        binding.etDpi.setText(String.valueOf(dpi));
        if (!check()) return;
        ShizukuHelper.setResolution(w, h, (ok, out) -> {});
        ShizukuHelper.setDpi(dpi, (ok, out) ->
            requireActivity().runOnUiThread(() ->
                toast(ok ? "Preset " + w + "x" + h + " diterapkan" : "Gagal")));
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
