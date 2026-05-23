package com.drizzx.project;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.drizzx.project.databinding.ActivityMainBinding;
import com.drizzx.project.fragment.DebugFragment;
import com.drizzx.project.fragment.LauncherFragment;
import com.drizzx.project.fragment.ProfileFragment;
import com.drizzx.project.fragment.SensiUpFragment;
import com.drizzx.project.fragment.TweaksFragment;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int currentNav = 4;

    private final Shizuku.OnRequestPermissionResultListener permListener =
        (code, result) -> {
            if (code == ShizukuHelper.SHIZUKU_CODE) {
                runOnUiThread(this::updateShizukuStatus);
                if (result == PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(() -> Toast.makeText(this, "Shizuku berhasil diizinkan", Toast.LENGTH_SHORT).show());
                }
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
        loadFragment(new ProfileFragment(), 4);
        updateShizukuStatus();
    }

    private void setupNavigation() {
        binding.navLauncher.setOnClickListener(v -> loadFragment(new LauncherFragment(), 0));
        binding.navTweaks.setOnClickListener(v -> loadFragment(new TweaksFragment(), 1));
        binding.navSensiup.setOnClickListener(v -> loadFragment(new SensiUpFragment(), 2));
        binding.navDebug.setOnClickListener(v -> loadFragment(new DebugFragment(), 3));
        binding.navProfile.setOnClickListener(v -> loadFragment(new ProfileFragment(), 4));
    }

    private void loadFragment(Fragment fragment, int navIndex) {
        currentNav = navIndex;
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
        updateNavSelection(navIndex);
    }

    private void updateNavSelection(int index) {
        int cyan = getColor(R.color.cyan);
        int dim = getColor(R.color.text_dim);

        binding.navLauncher.setTextColor(index == 0 ? cyan : dim);
        binding.navIconLauncher.setColorFilter(index == 0 ? cyan : dim);

        binding.navTweaks.setTextColor(index == 1 ? cyan : dim);
        binding.navIconTweaks.setColorFilter(index == 1 ? cyan : dim);

        binding.navSensiup.setTextColor(index == 2 ? cyan : dim);
        binding.navIconSensiup.setColorFilter(index == 2 ? cyan : dim);

        binding.navDebug.setTextColor(index == 3 ? cyan : dim);
        binding.navIconDebug.setColorFilter(index == 3 ? cyan : dim);

        binding.navProfile.setTextColor(index == 4 ? cyan : dim);
        binding.navIconProfile.setColorFilter(index == 4 ? cyan : dim);
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
            if (!running) Toast.makeText(this, "Install dan jalankan Shizuku", Toast.LENGTH_SHORT).show();
            else if (!hasPerm) ShizukuHelper.requestPermission();
            else Toast.makeText(this, "Shizuku sudah aktif", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(permListener);
        Shizuku.removeBinderReceivedListener(binderReceived);
        Shizuku.removeBinderDeadListener(binderDead);
    }
}
