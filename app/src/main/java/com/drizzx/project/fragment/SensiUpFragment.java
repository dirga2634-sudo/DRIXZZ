package com.drizzx.project.fragment;

import android.os.Bundle;
import android.util.DisplayMetrics;
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
    private int deviceW, deviceH, deviceDpi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSensiupBinding.inflate(inflater, container, false);
        pref = new PrefManager(requireContext());

        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        deviceW = dm.widthPixels;
        deviceH = dm.heightPixels;
        deviceDpi = dm.densityDpi;

        binding.tvCurrentRes.setText(deviceW + " x " + deviceH + " (" + deviceDpi + " DPI)");

        int savedW = pref.getWidth(deviceW);
        int savedH = pref.getHeight(deviceH);
        int savedDpi = pref.getDpi(deviceDpi);
        binding.etWidth.setText(String.valueOf(savedW));
        binding.etHeight.setText(String.valueOf(savedH));
        binding.etDpi.setText(String.valueOf(savedDpi));

        setupButtons();
        return binding.getRoot();
    }

    private void setupButtons() {
        binding.btnApplyRes.setOnClickListener(v -> {
            if (!check()) return;
            String ws = binding.etWidth.getText().toString().trim();
            String hs = binding.etHeight.getText().toString().trim();
            String ds = binding.etDpi.getText().toString().trim();
            if (ws.isEmpty() || hs.isEmpty() || ds.isEmpty()) { toast("Isi semua field"); return; }
            int w = Integer.parseInt(ws);
            int h = Integer.parseInt(hs);
            int d = Integer.parseInt(ds);
            pref.setWidth(w); pref.setHeight(h); pref.setDpi(d);
            ShizukuHelper.setResolution(w, h, (ok, out) -> {});
            ShizukuHelper.setDpi(d, (ok, out) ->
                requireActivity().runOnUiThread(() ->
                    toast(ok ? "Diterapkan: " + w + "x" + h + " @ " + d + " DPI" : "Gagal: " + out)));
        });

        binding.btnResetRes.setOnClickListener(v -> {
            if (!check()) return;
            pref.setWidth(deviceW); pref.setHeight(deviceH); pref.setDpi(deviceDpi);
            binding.etWidth.setText(String.valueOf(deviceW));
            binding.etHeight.setText(String.valueOf(deviceH));
            binding.etDpi.setText(String.valueOf(deviceDpi));
            ShizukuHelper.resetResolution((ok, out) -> {});
            ShizukuHelper.resetDpi((ok, out) ->
                requireActivity().runOnUiThread(() ->
                    toast(ok ? "Direset ke default" : "Gagal: " + out)));
        });

        binding.btnPreset540.setOnClickListener(v -> applyPreset(540, 1200, 160));
        binding.btnPreset720.setOnClickListener(v -> applyPreset(720, 1600, 230));
        binding.btnPreset1080.setOnClickListener(v -> applyPreset(1080, 2400, 395));
    }

    private void applyPreset(int w, int h, int dpi) {
        binding.etWidth.setText(String.valueOf(w));
        binding.etHeight.setText(String.valueOf(h));
        binding.etDpi.setText(String.valueOf(dpi));
        pref.setWidth(w); pref.setHeight(h); pref.setDpi(dpi);
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

    private void toast(String msg) { Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
