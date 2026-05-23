package com.drizzx.project;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    private static final String PREF_NAME = "drizzx_prefs";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public PrefManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Sensitivity
    public void setSensLevel(int level) { editor.putInt("sens_level", level).apply(); }
    public int getSensLevel() { return prefs.getInt("sens_level", 4); }

    // Tweaks switches
    public void setMouseConfig(boolean v) { editor.putBoolean("mouse_config", v).apply(); }
    public boolean getMouseConfig() { return prefs.getBoolean("mouse_config", false); }

    public void setSensEnabled(boolean v) { editor.putBoolean("sens_enabled", v).apply(); }
    public boolean getSensEnabled() { return prefs.getBoolean("sens_enabled", false); }

    public void setSurfaceFlinger(boolean v) { editor.putBoolean("surfaceflinger", v).apply(); }
    public boolean getSurfaceFlinger() { return prefs.getBoolean("surfaceflinger", false); }

    public void setGpu(boolean v) { editor.putBoolean("gpu", v).apply(); }
    public boolean getGpu() { return prefs.getBoolean("gpu", false); }

    // Resolution
    public void setWidth(int v) { editor.putInt("res_width", v).apply(); }
    public void setHeight(int v) { editor.putInt("res_height", v).apply(); }
    public void setDpi(int v) { editor.putInt("res_dpi", v).apply(); }
    public int getWidth(int def) { return prefs.getInt("res_width", def); }
    public int getHeight(int def) { return prefs.getInt("res_height", def); }
    public int getDpi(int def) { return prefs.getInt("res_dpi", def); }

    // Performance mode
    public void setPerfMode(String v) { editor.putString("perf_mode", v).apply(); }
    public String getPerfMode() { return prefs.getString("perf_mode", "balanced"); }
}
