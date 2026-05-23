package com.drizzx.project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.drizzx.project.PrefManager;
import com.drizzx.project.ShizukuHelper;
import com.drizzx.project.databinding.FragmentSensiupBinding;

public class SensiUpFragment extends Fragment {

    private FragmentSensiupBinding binding;
    private PrefManager pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSensiupBinding.inflate(inflater, container, false);
        pref = new PrefManager(requireContext());
        setupResolution();
        return binding.getRoot();
    }

    private void setupResolution() {
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        // Load saved values or use current
        int savedW = pref.getWidth(dm.widthPixels);
        int savedH = pref.getHeight(dm.heightPixels);
        int savedDpi = pref.getDpi(dm.densityDpi);

        binding.tvCurrentRes.setText(dm.widthPixels + " x " + dm.heightPixels + " (" + dm.densityDpi + " DPI)");
        binding.etWidth.setText(String.valueOf(savedW));
        binding.etHeight.setText(String.valueOf(savedH));
        binding.etDpi.setText(String.valueOf(savedDpi));

        // Apply
        binding.btnApplyRes.setOnClickListener(v -> {
            if (!check()) return;
            String ws = binding.etWidth.getText().toString().trim();
            String hs = binding.etHeight.getText().toString().trim();
            String ds = binding.etDpi.getText().toString().trim();
            if (ws.isEmpty() || hs.isEmpty() || ds.isEmpty()) { toast("Isi semua field"); return; }

            int w = Integer.parseInt(ws);
            int h = Integer.parseInt(hs);
            int d = Integer.parseInt(ds);

            // Save to prefs
            pref.setWidth(w);
            pref.setHeight(h);
            pref.setDpi(d);

            ShizukuHelper.setResolution(w, h, (ok, out) -> {});
            ShizukuHelper.setDpi(d, (ok, out) ->
                requireActivity().runOnUiThread(() ->
                    toast(ok ? "Diterapkan: " + w + "x" + h + " @ " + d + "dpi" : "Gagal: " + out)));
        });

        // Reset
        binding.btnResetRes.setOnClickListener(v -> {
            if (!check()) return;
            pref.setWidth(dm.widthPixels);
            pref.setHeight(dm.heightPixels);
            pref.setDpi(dm.densityDpi);
            binding.etWidth.setText(String.valueOf(dm.widthPixels));
            binding.etHeight.setText(String.valueOf(dm.heightPixels));
            binding.etDpi.setText(String.valueOf(dm.densityDpi));
            ShizukuHelper.resetResolution((ok, out) -> {});
            ShizukuHelper.resetDpi((ok, out) ->
                requireActivity().runOnUiThread(() ->
                    toast(ok ? "Resolusi & DPI direset ke default" : "Gagal: " + out)));
        });

        // Presets
        binding.btnPreset540.setOnClickListener(v -> applyPreset(540, 1200, 160));
        binding.btnPreset720.setOnClickListener(v -> applyPreset(720, 1600, 230));
        binding.btnPreset1080.setOnClickListener(v -> applyPreset(1080, 2400, 395));
    }

    private void applyPreset(int w, int h, int dpi) {
        binding.etWidth.setText(String.valueOf(w));
        binding.etHeight.setText(String.valueOf(h));
        binding.etDpi.setText(String.valueOf(dpi));
        pref.setWidth(w);
        pref.setHeight(h);
        pref.setDpi(dpi);
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

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
