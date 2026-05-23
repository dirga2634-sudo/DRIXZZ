package com.drizzx.project.fragment;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.drizzx.project.ShizukuHelper;
import com.drizzx.project.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        loadDeviceInfo();
        updateShizuku();
        return binding.getRoot();
    }

    private void loadDeviceInfo() {
        // Real device info
        String brand = Build.MANUFACTURER;
        String model = Build.MODEL;
        String android = Build.VERSION.RELEASE;
        int sdk = Build.VERSION.SDK_INT;
        String abi = Build.SUPPORTED_ABIS[0];
        String kernel = System.getProperty("os.version", "-");

        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        String resolution = dm.widthPixels + "x" + dm.heightPixels + " (" + dm.densityDpi + " DPI)";

        binding.tvPhone.setText(brand + " | " + model);
        binding.tvAndroid.setText(android + " | SDK " + sdk);
        binding.tvResolution.setText(resolution);
        binding.tvAbi.setText(abi + " | " + kernel);
        binding.tvBrand.setText(brand);
    }

    private void updateShizuku() {
        boolean running = ShizukuHelper.isRunning();
        boolean hasPerm = ShizukuHelper.hasPermission();

        if (!running) {
            binding.tvShizukuInfo.setText("Izin Shizuku diperlukan");
            binding.tvShizukuVersion.setText("Shizuku belum aktif");
            binding.btnShizuku.setVisibility(View.VISIBLE);
        } else if (!hasPerm) {
            binding.tvShizukuInfo.setText("Tap untuk izinkan Shizuku");
            binding.tvShizukuVersion.setText("Menunggu izin...");
            binding.btnShizuku.setVisibility(View.VISIBLE);
        } else {
            binding.tvShizukuInfo.setText("Shizuku berfungsi");
            binding.tvShizukuVersion.setText("Version: 13.x - Connected");
            binding.btnShizuku.setVisibility(View.GONE);
        }

        binding.btnShizuku.setOnClickListener(v -> {
            if (!running) {
                Toast.makeText(requireContext(), "Install dan jalankan Shizuku terlebih dahulu", Toast.LENGTH_SHORT).show();
            } else {
                ShizukuHelper.requestPermission();
            }
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
