package com.drizzx.one.free;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.drizzx.one.free.databinding.ActivityMenuBinding;
import com.drizzx.one.free.databinding.DialogInputBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Menu Tools");
        }

        setupMenuItems();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupMenuItems() {

        binding.itemOpenGame.setOnClickListener(v ->
                startActivity(new Intent(this, OpengameActivity.class)));

        binding.itemDebug.setOnClickListener(v ->
                startActivity(new Intent(this, DebugActivity.class)));

        binding.itemDeviceInfo.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            showToast("⏳ Loading...");
            ShizukuHelper.getDeviceInfo((ok, out) ->
                    runOnUiThread(() -> showResultDialog("📱 Device Info", out)));
        });

        binding.itemListPackages.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            showToast("⏳ Loading packages...");
            ShizukuHelper.listPackages((ok, out) -> runOnUiThread(() -> {
                if (ok) showResultDialog("📦 Packages", out);
                else showToast("❌ " + out);
            }));
        });

        binding.itemGrantPerm.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            showGrantDialog();
        });

        binding.itemGrantAll.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            showInputDialog("Grant All Permissions", "Package name", pkg ->
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("⚠️ Grant All?")
                            .setMessage("Grant SEMUA permission ke " + pkg + "?")
                            .setNegativeButton("Batal", null)
                            .setPositiveButton("Grant", (d, w) -> {
                                showToast("⏳ Granting...");
                                ShizukuHelper.grantAllDangerousPermissions(pkg, (ok, out) ->
                                        runOnUiThread(() -> showToast(ok ? "✅ Done!" : "❌ " + out)));
                            }).show());
        });

        binding.itemForceStop.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            showInputDialog("Force Stop", "Package name", pkg ->
                    ShizukuHelper.forceStop(pkg, (ok, out) ->
                            runOnUiThread(() -> showToast(ok ? "✅ Stopped: " + pkg : "❌ " + out))));
        });

        binding.itemClearData.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            showInputDialog("Clear Data", "Package name", pkg ->
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("⚠️ Konfirmasi")
                            .setMessage("Clear data " + pkg + "?")
                            .setNegativeButton("Batal", null)
                            .setPositiveButton("Clear", (d, w) ->
                                    ShizukuHelper.clearData(pkg, (ok, out) ->
                                            runOnUiThread(() -> showToast(ok ? "✅ Cleared!" : "❌ " + out))))
                            .show());
        });

        binding.itemDisableApp.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            showInputDialog("Disable App", "Package name", pkg ->
                    ShizukuHelper.disableApp(pkg, (ok, out) ->
                            runOnUiThread(() -> showToast(ok ? "✅ Disabled: " + pkg : "❌ " + out))));
        });

        binding.itemEnableApp.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            showInputDialog("Enable App", "Package name", pkg ->
                    ShizukuHelper.enableApp(pkg, (ok, out) ->
                            runOnUiThread(() -> showToast(ok ? "✅ Enabled: " + pkg : "❌ " + out))));
        });

        binding.itemLaunchApp.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            showInputDialog("Launch App", "Package name", pkg ->
                    ShizukuHelper.launchApp(pkg, (ok, out) ->
                            runOnUiThread(() -> showToast(ok ? "🚀 Launching!" : "❌ " + out))));
        });

        binding.itemShellCmd.setOnClickListener(v -> {
            if (!checkShizuku()) return;
            showInputDialog("Shell Command", "Ketik command (sh -c ...)", cmd -> {
                showToast("⏳ Running...");
                ShizukuHelper.runCommand(cmd, (ok, out) ->
                        runOnUiThread(() -> showResultDialog(ok ? "✅ Output" : "❌ Error", out)));
            });
        });
    }

    interface OnInput { void onInput(String s); }

    private void showInputDialog(String title, String hint, OnInput cb) {
        DialogInputBinding d = DialogInputBinding.inflate(LayoutInflater.from(this));
        d.tilInput1.setHint(hint);
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(d.getRoot())
                .setNegativeButton("Batal", null)
                .setPositiveButton("OK", (dialog, w) -> {
                    String val = d.etInput1.getText().toString().trim();
                    if (!val.isEmpty()) cb.onInput(val);
                    else showToast("Jangan kosong!");
                }).show();
    }

    private void showGrantDialog() {
        DialogInputBinding d = DialogInputBinding.inflate(LayoutInflater.from(this));
        d.tilInput1.setHint("Package Name");
        d.tilInput2.setHint("Permission (contoh: android.permission.READ_CONTACTS)");
        d.tilInput2.setVisibility(View.VISIBLE);
        new MaterialAlertDialogBuilder(this)
                .setTitle("🔓 Grant Permission")
                .setView(d.getRoot())
                .setNegativeButton("Batal", null)
                .setPositiveButton("Grant", (dialog, w) -> {
                    String pkg = d.etInput1.getText().toString().trim();
                    String perm = d.etInput2.getText().toString().trim();
                    if (pkg.isEmpty() || perm.isEmpty()) { showToast("Isi semua!"); return; }
                    ShizukuHelper.grantPermission(pkg, perm, (ok, out) ->
                            runOnUiThread(() -> showToast(out)));
                }).show();
    }

    private void showResultDialog(String title, String content) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(content.isEmpty() ? "(kosong)" : content)
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean checkShizuku() {
        if (!ShizukuHelper.isRunning()) {
            showToast("⚠️ Shizuku belum aktif!");
            return false;
        }
        if (!ShizukuHelper.hasPermission()) {
            ShizukuHelper.requestPermission();
            showToast("⚠️ Izinkan permission Shizuku!");
            return false;
        }
        return true;
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
