package com.drizzx.project;

import android.content.pm.PackageManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import rikka.shizuku.Shizuku;

public class ShizukuHelper {

    private static final String TAG = "DRIZZX";
    public static final int SHIZUKU_CODE = 1001;

    public interface Callback {
        void onResult(boolean success, String output);
    }

    public static boolean isRunning() {
        try { return Shizuku.pingBinder(); } catch (Exception e) { return false; }
    }

    public static boolean hasPermission() {
        try {
            if (Shizuku.isPreV11()) return false;
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) { return false; }
    }

    public static void requestPermission() {
        try {
            if (!Shizuku.isPreV11()) Shizuku.requestPermission(SHIZUKU_CODE);
        } catch (Exception e) { Log.e(TAG, e.getMessage()); }
    }

    private static byte[] readStream(InputStream is) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) b.write(buf, 0, n);
        return b.toByteArray();
    }

    public static void run(String cmd, Callback cb) {
        if (!isRunning()) { if (cb != null) cb.onResult(false, "Shizuku tidak aktif"); return; }
        if (!hasPermission()) { if (cb != null) cb.onResult(false, "Tidak ada permission"); return; }
        new Thread(() -> {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
                String out = new String(readStream(p.getInputStream())).trim();
                p.waitFor();
                if (cb != null) cb.onResult(true, out);
            } catch (Exception e) {
                if (cb != null) cb.onResult(false, e.getMessage());
            }
        }).start();
    }

    public static void grantPerm(String pkg, String perm, Callback cb) {
        run("pm grant " + pkg + " " + perm, cb);
    }

    public static void revokePerm(String pkg, String perm, Callback cb) {
        run("pm revoke " + pkg + " " + perm, cb);
    }

    public static void forceStop(String pkg, Callback cb) {
        run("am force-stop " + pkg, cb);
    }

    public static void clearData(String pkg, Callback cb) {
        run("pm clear " + pkg, cb);
    }

    public static void disableApp(String pkg, Callback cb) {
        run("pm disable-user --user 0 " + pkg, cb);
    }

    public static void enableApp(String pkg, Callback cb) {
        run("pm enable " + pkg, cb);
    }

    public static void listPackages(Callback cb) {
        run("pm list packages", cb);
    }

    public static void getDeviceInfo(Callback cb) {
        run("echo \"Android: $(getprop ro.build.version.release)\" && " +
            "echo \"SDK: $(getprop ro.build.version.sdk)\" && " +
            "echo \"Model: $(getprop ro.product.model)\" && " +
            "echo \"Brand: $(getprop ro.product.brand)\" && " +
            "echo \"Screen: $(wm size)\" && " +
            "echo \"DPI: $(wm density)\"", cb);
    }

    public static void setDpi(int dpi, Callback cb) {
        run("wm density " + dpi, cb);
    }

    public static void resetDpi(Callback cb) {
        run("wm density reset", cb);
    }

    public static void setResolution(int w, int h, Callback cb) {
        run("wm size " + w + "x" + h, cb);
    }

    public static void resetResolution(Callback cb) {
        run("wm size reset", cb);
    }

    public static void setSurfaceFlinger(boolean enable, Callback cb) {
        String val = enable ? "1" : "0";
        run("service call SurfaceFlinger 1008 i32 " + val, cb);
    }

    public static void setSensitivity(int level, Callback cb) {
        run("settings put system pointer_speed " + level, cb);
    }

    public static void cleanCache(Callback cb) {
        run("pm trim-caches 1000G", cb);
    }

    public static void launchApp(String pkg, Callback cb) {
        run("monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1", cb);
    }
}
