package com.drizzx.project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.drizzx.project.R;
import com.drizzx.project.ShizukuHelper;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvPhone = view.findViewById(R.id.tv_phone);
        TextView tvAndroid = view.findViewById(R.id.tv_android);
        TextView tvResolution = view.findViewById(R.id.tv_resolution);
        TextView tvAbi = view.findViewById(R.id.tv_abi);
        TextView tvShizukuStatus = view.findViewById(R.id.tv_shizuku_info);
        View btnShizuku = view.findViewById(R.id.btn_shizuku);

        tvPhone.setText(android.os.Build.MANUFACTURER + " | " + android.os.Build.MODEL);
        tvAndroid.setText(android.os.Build.VERSION.RELEASE + " | SDK " + android.os.Build.VERSION.SDK_INT);
        tvAbi.setText(android.os.Build.SUPPORTED_ABIS[0] + " | " + System.getProperty("os.version"));

        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        tvResolution.setText(dm.widthPixels + "x" + dm.heightPixels + " (" + dm.densityDpi + " DPI)");

        updateShizuku(tvShizukuStatus, btnShizuku);

        return view;
    }

    private void updateShizuku(TextView tv, View btn) {
        boolean running = ShizukuHelper.isRunning();
        boolean hasPerm = ShizukuHelper.hasPermission();

        if (!running) {
            tv.setText("Izin Shizuku diperlukan");
            btn.setVisibility(View.VISIBLE);
        } else if (!hasPerm) {
            tv.setText("Tap untuk izinkan Shizuku");
            btn.setVisibility(View.VISIBLE);
        } else {
            tv.setText("Shizuku berfungsi");
            btn.setVisibility(View.GONE);
        }

        btn.setOnClickListener(v -> {
            if (!running) {
                android.widget.Toast.makeText(requireContext(),
                    "Install dan jalankan Shizuku terlebih dahulu", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                ShizukuHelper.requestPermission();
            }
        });
    }
}
