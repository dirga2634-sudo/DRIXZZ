package com.drizzx.one.free;

import android.content.pm.PackageManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuRemoteProcess;

public class ShizukuHelper {

    private static final String TAG = "DRIZZX_SHIZUKU";
    public static final int SHIZUKU_CODE = 1001;

    public interface ShizukuCallback {
        void onResult(boolean success, String output);
    }

    public static boolean isRunning() {
        try {
            return Shizuku.pingBinder();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasPermission() {
        try {
            if (Shizuku.isPreV11()) return false;
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            return false;
        }
    }

    public static void requestPermission() {
        try {
            if (Shizuku.isPreV11()) return;
            Shizuku.requestPermission(SHIZUKU_CODE);
        } catch (Exception e) {
            Log.e(TAG, "requestPermission: " + e.getMessage());
        }
    }

    private static byte[] readStream(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
        return baos.toByteArray();
    }

    public static void runCommand(String command, ShizukuCallback callback) {
        if (!isRunning()) {
            if (callback != null) callback.onResult(false, "Shizuku tidak berjalan");
            return;
        }
        if (!hasPermission()) {
            if (callback != null) callback.onResult(false, "Tidak ada permission Shizuku");
            return;
        }
        new Thread(() -> {
            try {
                ShizukuRemoteProcess process = Shizuku.newProcess(
                        new String[]{"sh", "-c", command}, null, null);
                byte[] bytes = readStream(process.getInputStream());
                process.waitFor();
                String output = new String(bytes).trim();
                if (callback != null) callback.onResult(true, output);
            } catch (Exception e) {
                if (callback != null) callback.onResult(false, "Error: " + e.getMessage());
            }
        }).start();
    }

    public static void grantPermission(String pkg, String perm, ShizukuCallback cb) {
        runCommand("pm grant " + pkg + " " + perm, (ok, out) -> {
            if (cb != null) cb.onResult(ok, ok ? "Granted: " + perm : "Gagal: " + out);
        });
    }

    public static void revokePermission(String pkg, String perm, ShizukuCallback cb) {
        runCommand("pm revoke " + pkg + " " + perm, cb);
    }

    public static void grantAllDangerousPermissions(String pkg, ShizukuCallback cb) {
        runCommand("pm grant " + pkg +
                " $(pm list permissions -d -g | grep 'permission:' | sed 's/permission://' | tr '\\n' ' ')", cb);
    }

    public static void listPackages(ShizukuCallback cb) {
        runCommand("pm list packages", cb);
    }

    public static void listSystemPackages(ShizukuCallback cb) {
        runCommand("pm list packages -s", cb);
    }

    public static void disableApp(String pkg, ShizukuCallback cb) {
        runCommand("pm disable-user --user 0 " + pkg, cb);
    }

    public static void enableApp(String pkg, ShizukuCallback cb) {
        runCommand("pm enable " + pkg, cb);
    }

    public static void forceStop(String pkg, ShizukuCallback cb) {
        runCommand("am force-stop " + pkg, cb);
    }

    public static void clearData(String pkg, ShizukuCallback cb) {
        runCommand("pm clear " + pkg, cb);
    }

    public static void installApk(String path, ShizukuCallback cb) {
        runCommand("pm install -r -t " + path, cb);
    }

    public static void uninstallPackage(String pkg, ShizukuCallback cb) {
        runCommand("pm uninstall " + pkg, cb);
    }

    public static void getDeviceInfo(ShizukuCallback cb) {
        runCommand("echo \"Android: $(getprop ro.build.version.release)\" && " +
                "echo \"SDK: $(getprop ro.build.version.sdk)\" && " +
                "echo \"Model: $(getprop ro.product.model)\" && " +
                "echo \"Brand: $(getprop ro.product.brand)\" && " +
                "echo \"Screen: $(wm size)\" && " +
                "echo \"Battery: $(dumpsys battery | grep level | tr -d ' ')\"", cb);
    }

    public static void launchApp(String pkg, ShizukuCallback cb) {
        runCommand("monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1", cb);
    }
}
