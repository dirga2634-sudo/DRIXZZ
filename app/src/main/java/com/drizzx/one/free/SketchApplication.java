package com.drizzx.one.free;

import android.app.Application;
import android.util.Log;

public class SketchApplication extends Application {
    private static SketchApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.i("DRIZZX", "Started: " + getPackageName());
    }

    public static SketchApplication getInstance() {
        return instance;
    }
}
