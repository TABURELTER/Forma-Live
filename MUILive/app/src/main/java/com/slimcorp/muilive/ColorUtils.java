package com.slimcorp.muilive;

import android.graphics.Color;

public class ColorUtils {

    public static int getLightVariant(int baseColor, float valuePercent) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[1] = Math.min(1.0f, hsv[1] * 0.8f);
        hsv[2] = valuePercent;
        return Color.HSVToColor(hsv);
    }

    public static int getDarkVariant(int baseColor, float valuePercent) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[1] = Math.min(1.0f, hsv[1] * 1.1f);
        hsv[2] = valuePercent;
        return Color.HSVToColor(hsv);
    }

    public static int getCardVariant(int baseColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[1] = Math.min(1.0f, hsv[1] * 1.1f);
        hsv[2] = 0.25f;
        return Color.HSVToColor(hsv);
    }

    public static int getTextColor(int baseColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[1] = Math.min(1.0f, hsv[1] * 0.2f);
        hsv[2] = 0.95f;
        return Color.HSVToColor(hsv);
    }
}