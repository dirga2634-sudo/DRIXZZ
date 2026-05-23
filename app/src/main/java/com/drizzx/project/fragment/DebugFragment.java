package com.drizzx.project.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.fragment.app.Fragment;

import com.drizzx.project.ShizukuHelper;
import com.drizzx.project.databinding.FragmentDebugBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DebugFragment extends Fragment {

    private FragmentDebugBinding binding;
    private final StringBuilder log = new StringBuilder();
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDebugBinding.inflate(inflater, container, false);
        printInit();
        setupButtons();
        updateBadge();
        return binding.getRoot();
    }

    private void setupButtons() {
        binding.btnRun.setOnClickListener(v -> {
            String cmd = binding.etCommand.getText().toString().trim();
            if (cmd.isEmpty()) return;
            runCmd(cmd);
        });

        binding.etCommand.setOnEditorActionListener((v, id, e) -> {
            if (id == EditorInfo.IME_ACTION_DONE) {
                binding.btnRun.performClick();
                return true;
            }
            return false;
        });

        binding.btnClear.setOnClickListener(v -> {
            log.setLength(0);
            binding.tvConsole.setText("");
            addLog("SYS", "Console dibersihkan");
        });

        binding.btnDevInfo.setOnClickListener(v -> {
            if (!check()) return;
            addLog("CMD", "$ Device Info");
            ShizukuHelper.getDeviceInfo((ok, out) ->
                requireActivity().runOnUiThread(() ->
                    addLog(ok ? "OUT" : "ERR", out)));
        });

        binding.btnListPkg.setOnClickListener(v -> {
            if (!check()) return;
            addLog("CMD", "$ pm list packages");
            ShizukuHelper.listPackages((ok, out) -> requireActivity().runOnUiThread(() -> {
                if (!ok) { addLog("ERR", out); return; }
                String[] lines = out.split("\n");
                int max = Math.min(lines.length, 30);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < max; i++) sb.append(lines[i]).append("\n");
                if (lines.length > 30) sb.append("... +").append(lines.length - 30).append(" lainnya");
                addLog("OUT", sb.toString().trim());
            }));
        });

        binding.btnWhoami.setOnClickListener(v -> {
            if (!check()) return;
            runCmd("whoami && id");
        });
    }

    private void printInit() {
        addLog("SYS", "=== DRIZZX PROJECT Debug Console ===");
        addLog("INF", "Package: com.drizzx.project");
        addLog("INF", "Device: " + Build.MANUFACTURER + " " + Build.MODEL);
        addLog("INF", "Android: " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")");
        addLog("INF", "ABI: " + Build.SUPPORTED_ABIS[0]);
        addLog(ShizukuHelper.isRunning() ? "INF" : "WRN",
            "Shizuku: " + (ShizukuHelper.isRunning() ? "Aktif" : "Tidak Aktif"));
        addLog(ShizukuHelper.hasPermission() ? "INF" : "WRN",
            "Permission: " + (ShizukuHelper.hasPermission() ? "OK" : "Belum diberikan"));
        addLog("SYS", "Siap. Ketik command di bawah.");
    }

    private void updateBadge() {
        boolean ok = ShizukuHelper.isRunning() && ShizukuHelper.hasPermission();
        binding.tvShizukuBadge.setText(ok ? "Shizuku: OK" : "Shizuku: OFF");
        binding.tvShizukuBadge.setTextColor(requireContext().getColor(ok ?
            com.drizzx.project.R.color.green : com.drizzx.project.R.color.red));
    }

    private void runCmd(String cmd) {
        if (!check()) return;
        binding.etCommand.setText("");
        addLog("CMD", "$ " + cmd);
        ShizukuHelper.run(cmd, (ok, out) ->
            requireActivity().runOnUiThread(() ->
                addLog(ok ? "OUT" : "ERR", out.isEmpty() ? "(tidak ada output)" : out)));
    }

    private void addLog(String level, String msg) {
        String time = sdf.format(new Date());
        log.append("[").append(time).append("][").append(level).append("] ").append(msg).append("\n");
        binding.tvConsole.setText(log.toString());
        binding.scrollConsole.post(() -> binding.scrollConsole.fullScroll(View.FOCUS_DOWN));
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

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
