package com.exam.novelt3_1;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DPIHelper {
    private int deviceWidth;
    private int deviceHeight;
    private float deviceLogicalDensity;

    private int density;
    private int dipWidth;
    private int dipHeight;

    private float xDPI;
    private float yDPI;

    public DPIHelper(Context context, Display display) {
        getDisplayInfo(context, display);
    }

    int getDeviceWidth() { return deviceWidth; }
    int getDeviceHeight() { return deviceHeight; }
    float getDeviceLogicalDensity() { return deviceLogicalDensity; }

    int getDensity() { return density; }
    int getDipWidth() { return dipWidth; }
    int getDipHeight() { return dipHeight; }

    public float getXDPI() { return xDPI; }
    public float getYDPI() { return yDPI; }

    int getDipPerCentimetre() {
        // 1 Inch = 2.54 Centimetre
        return (int)(getDensity() / 2.54);
    }

    int getDipPerMillimetre() {
        // 1 Inch = 25.4 Millimetre
        return (int)(getDensity() / 25.4);
    }

    private void getDisplayInfo(Context context, Display display) {
        DisplayMetrics displayMetrics = new DisplayMetrics();

        display.getMetrics(displayMetrics);

        deviceWidth = displayMetrics.widthPixels;
        deviceHeight = displayMetrics.heightPixels;

        display.getMetrics(displayMetrics);

        deviceLogicalDensity = displayMetrics.density;

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        // constant: DENSITY_LOW 120, DENSITY_MEDIUM 160, DENSITY_HIGH 240
        density = metrics.densityDpi;
        System.out.println("density : " + density);

        dipWidth = (int) (deviceWidth / displayMetrics.density);
        dipHeight = (int) (deviceHeight / displayMetrics.density);

        xDPI = metrics.xdpi;
        yDPI = metrics.ydpi;
    }
}
