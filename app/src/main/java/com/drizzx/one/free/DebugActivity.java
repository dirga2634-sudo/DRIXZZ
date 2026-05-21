package com.drizzx.one.free;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.drizzx.one.free.databinding.ActivityDebugBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DebugActivity extends AppCompatActivity {

    private ActivityDebugBinding binding;
    private final StringBuilder log = new StringBuilder();
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebugBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Debug Console");
        }

        printInit();
        updateBadge();
        setupButtons();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    private void setupButtons() {
        binding.btnRun.setOnClickListener(v -> {
            String cmd = binding.etCommand.getText().toString().trim();
            if (cmd.isEmpty()) { showToast("Ketik command dulu!"); return; }
            runCmd(cmd);
        });

        binding.etCommand.setOnEditorActionListener((v, id, e) -> {
            if (id == EditorInfo.IME_ACTION_DONE) { binding.btnRun.performClick(); return true; }
            return false;
        });

        binding.btnClear.setOnClickListener(v -> {
            log.setLength(0);
            binding.tvConsole.setText("");
            addLog("SYS", "Console cleared");
        });

        binding.btnDevInfo.setOnClickListener(v -> {
            if (!check()) return;
            addLog("CMD", "$ getprop (device info)");
            ShizukuHelper.getDeviceInfo((ok, out) ->
                    runOnUiThread(() -> addLog(ok ? "OUT" : "ERR", out)));
        });

        binding.btnListPkg.setOnClickListener(v -> {
            if (!check()) return;
            addLog("CMD", "$ pm list packages");
            ShizukuHelper.listPackages((ok, out) -> runOnUiThread(() -> {
                if (!ok) { addLog("ERR", out); return; }
                String[] lines = out.split("\n");
                int max = Math.min(lines.length, 20);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < max; i++) sb.append(lines[i]).append("\n");
                if (lines.length > 20) sb.append("... +").append(lines.length - 20).append(" more");
                addLog("OUT", sb.toString().trim());
            }));
        });

        binding.btnWhoami.setOnClickListener(v -> {
            if (!check()) return;
            runCmd("whoami && id");
        });

        binding.btnGrantSelf.setOnClickListener(v -> {
            if (!check()) return;
            addLog("CMD", "Grant SYSTEM_ALERT_WINDOW to self");
            ShizukuHelper.grantPermission(getPackageName(),
                    "android.permission.SYSTEM_ALERT_WINDOW",
                    (ok, out) -> runOnUiThread(() -> addLog(ok ? "OUT" : "ERR", out)));
        });
    }

    private void printInit() {
        addLog("SYS", "═══ DRIZZX Debug Console ═══");
        addLog("INF", "Package: " + getPackageName());
        addLog("INF", "Android SDK: " + android.os.Build.VERSION.SDK_INT);
        addLog("INF", "Device: " + android.os.Build.MODEL);
        addLog(ShizukuHelper.isRunning() ? "INF" : "WRN",
                "Shizuku running: " + ShizukuHelper.isRunning());
        addLog(ShizukuHelper.hasPermission() ? "INF" : "WRN",
                "Shizuku permission: " + ShizukuHelper.hasPermission());
        addLog("SYS", "Ready.");
    }

    private void updateBadge() {
        boolean ok = ShizukuHelper.isRunning() && ShizukuHelper.hasPermission();
        binding.tvShizukuBadge.setText(ok ? "● Shizuku OK" : "● Shizuku OFF");
        binding.tvShizukuBadge.setTextColor(getColor(ok ? R.color.green : R.color.red));
    }

    private void runCmd(String cmd) {
        if (!check()) return;
        binding.etCommand.setText("");
        addLog("CMD", "$ " + cmd);
        ShizukuHelper.runCommand(cmd, (ok, out) ->
                runOnUiThread(() -> addLog(ok ? "OUT" : "ERR", out.isEmpty() ? "(no output)" : out)));
    }

    private void addLog(String level, String msg) {
        String time = sdf.format(new Date());
        log.append("[").append(time).append("] [").append(level).append("] ").append(msg).append("\n");
        binding.tvConsole.setText(log.toString());
        binding.scrollConsole.post(() -> binding.scrollConsole.fullScroll(android.view.View.FOCUS_DOWN));
    }

    private boolean check() {
        if (!ShizukuHelper.isRunning()) { addLog("ERR", "Shizuku tidak aktif!"); return false; }
        if (!ShizukuHelper.hasPermission()) {
            addLog("WRN", "Butuh permission Shizuku...");
            ShizukuHelper.requestPermission();
            return false;
        }
        return true;
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
