package com.drizzx.project;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.drizzx.project.databinding.ActivityMainBinding;
import com.drizzx.project.fragment.DebugFragment;
import com.drizzx.project.fragment.LauncherFragment;
import com.drizzx.project.fragment.ProfileFragment;
import com.drizzx.project.fragment.TweaksFragment;
import com.drizzx.project.fragment.SensiUpFragment;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final Shizuku.OnRequestPermissionResultListener permListener =
        (code, result) -> {
            if (code == ShizukuHelper.SHIZUKU_CODE) {
                runOnUiThread(this::updateShizukuStatus);
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

        Shizuku.addRequestPermissionResultListener(permListener);
        Shizuku.addBinderReceivedListenerSticky(binderReceived);
        Shizuku.addBinderDeadListener(binderDead);

        setupNavigation();
        loadFragment(new ProfileFragment());
        updateShizukuStatus();
    }

    private void setupNavigation() {
        binding.navLauncher.setOnClickListener(v -> {
            selectNav(0);
            loadFragment(new LauncherFragment());
        });
        binding.navTweaks.setOnClickListener(v -> {
            selectNav(1);
            loadFragment(new TweaksFragment());
        });
        binding.navSensiup.setOnClickListener(v -> {
            selectNav(2);
            loadFragment(new SensiUpFragment());
        });
        binding.navDebug.setOnClickListener(v -> {
            selectNav(3);
            loadFragment(new DebugFragment());
        });
        binding.navProfile.setOnClickListener(v -> {
            selectNav(4);
            loadFragment(new ProfileFragment());
        });

        selectNav(4);
    }

    private void selectNav(int index) {
        int cyan = getColor(R.color.cyan);
        int dim = getColor(R.color.text_dim);

        binding.navLauncher.setTextColor(index == 0 ? cyan : dim);
        binding.navTweaks.setTextColor(index == 1 ? cyan : dim);
        binding.navSensiup.setTextColor(index == 2 ? cyan : dim);
        binding.navDebug.setTextColor(index == 3 ? cyan : dim);
        binding.navProfile.setTextColor(index == 4 ? cyan : dim);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }

    private void updateShizukuStatus() {
        boolean running = ShizukuHelper.isRunning();
        boolean hasPerm = ShizukuHelper.hasPermission();

        if (!running) {
            binding.shizukuStatus.setText("Shizuku: Tidak Aktif");
            binding.shizukuDot.setBackgroundResource(R.drawable.dot_red);
        } else if (!hasPerm) {
            binding.shizukuStatus.setText("Shizuku: Butuh Izin");
            binding.shizukuDot.setBackgroundResource(R.drawable.dot_yellow);
        } else {
            binding.shizukuStatus.setText("Shizuku: Aktif");
            binding.shizukuDot.setBackgroundResource(R.drawable.dot_green);
        }

        binding.shizukuCard.setOnClickListener(v -> {
            if (!running) showToast("Install dan jalankan Shizuku terlebih dahulu");
            else if (!hasPerm) ShizukuHelper.requestPermission();
            else showToast("Shizuku sudah aktif");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(permListener);
        Shizuku.removeBinderReceivedListener(binderReceived);
        Shizuku.removeBinderDeadListener(binderDead);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
