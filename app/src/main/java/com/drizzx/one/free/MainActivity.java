package com.drizzx.one.free;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.drizzx.one.free.databinding.ActivityMainBinding;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final Shizuku.OnRequestPermissionResultListener permissionListener =
            (requestCode, grantResult) -> {
                if (requestCode == ShizukuHelper.SHIZUKU_CODE) {
                    runOnUiThread(() -> {
                        updateShizukuStatus();
                        boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
                        showToast(granted ? "✅ Shizuku granted!" : "❌ Permission denied");
                    });
                }
            };

    private final Shizuku.OnBinderReceivedListener binderReceived = () ->
            runOnUiThread(() -> {
                updateShizukuStatus();
                if (!ShizukuHelper.hasPermission()) ShizukuHelper.requestPermission();
            });

    private final Shizuku.OnBinderDeadListener binderDead = () ->
            runOnUiThread(this::updateShizukuStatus);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Shizuku.addRequestPermissionResultListener(permissionListener);
        Shizuku.addBinderReceivedListenerSticky(binderReceived);
        Shizuku.addBinderDeadListener(binderDead);

        binding.btnOpengame.setOnClickListener(v ->
                startActivity(new Intent(this, OpengameActivity.class)));

        binding.btnMenu.setOnClickListener(v ->
                startActivity(new Intent(this, MenuActivity.class)));

        binding.btnDebug.setOnClickListener(v ->
                startActivity(new Intent(this, DebugActivity.class)));

        binding.cardShizuku.setOnClickListener(v -> {
            if (!ShizukuHelper.isRunning()) {
                showToast("⚠️ Install & jalankan Shizuku dulu!");
            } else if (!ShizukuHelper.hasPermission()) {
                ShizukuHelper.requestPermission();
            } else {
                showToast("✅ Shizuku aktif & ready");
            }
        });

        updateShizukuStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateShizukuStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(permissionListener);
        Shizuku.removeBinderReceivedListener(binderReceived);
        Shizuku.removeBinderDeadListener(binderDead);
    }

    private void updateShizukuStatus() {
        boolean running = ShizukuHelper.isRunning();
        boolean hasPerm = ShizukuHelper.hasPermission();

        if (!running) {
            binding.tvShizukuStatus.setText("Shizuku: Tidak aktif");
            binding.ivShizukuDot.setBackgroundResource(R.drawable.dot_red);
        } else if (!hasPerm) {
            binding.tvShizukuStatus.setText("Shizuku: Butuh permission");
            binding.ivShizukuDot.setBackgroundResource(R.drawable.dot_yellow);
        } else {
            binding.tvShizukuStatus.setText("Shizuku: Connected ✓");
            binding.ivShizukuDot.setBackgroundResource(R.drawable.dot_green);
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
