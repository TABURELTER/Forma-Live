package com.slimcorp.muilive;

import android.content.Context;
import android.graphics.Color;
import com.google.android.material.color.MaterialColors;

public class ThemeColors {
    public final int primary, secondary, surface, tertiary;

    public ThemeColors(Context ctx) {
        primary = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorPrimary, Color.parseColor("#6750A4"));
        secondary = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorSecondary, Color.parseColor("#625B71"));
        tertiary = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorTertiary, Color.parseColor("#7D5260"));
        surface = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorSurface, Color.parseColor("#FFFBFE"));
    }

    public static int lighten(int color, float factor) {
        int r = (int) (Color.red(color) + (255 - Color.red(color)) * factor);
        int g = (int) (Color.green(color) + (255 - Color.green(color)) * factor);
        int b = (int) (Color.blue(color) + (255 - Color.blue(color)) * factor);
        return Color.rgb(Math.min(255, r), Math.min(255, g), Math.min(255, b));
    }

    public static int darken(int color, float factor) {
        int r = (int) (Color.red(color) * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color) * factor);
        return Color.rgb(r, g, b);
    }
}