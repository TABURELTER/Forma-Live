package com.slimcorp.muilive;

import android.content.Context;
import android.content.SharedPreferences;

public class ShapeConfig {
    public boolean useDynamicColors = true;
    public float rotationSpeed = 1.4f;
    public float petalDepth = 0.15f;

    public int shapeCount = 2;
    public float scaleX = 0.70f;
    public float scaleY = 0.70f;
    public float sizeMultiplier = 0.30f;

    public int shapeMask = 127;
    public int backgroundColor = 0xFF151122;
    public int secondaryColor = 0xFFFFAFA3;
    public boolean customBackground = false;
    public boolean useSecondary = false;
    public float opacity = 1f;
    public float blur = 0f;
    public float spacing = 1f;
    public int seed = 761;
    public int extra2 = 0xFF82B1FF;
    public int extra3 = 0xFFFFD180;
    public boolean pulse = false;
    public int extraCount = 1;
    public String photo = "";
    public int baseColor = 0xFF6750A4;

    private static final String PREFS = "material_shapes_prefs";

    public void reset() {
        useDynamicColors = true;
        rotationSpeed = 1.4f;
        petalDepth = 0.15f;
        sizeMultiplier = 0.30f;
        shapeMask = 127;
        backgroundColor = 0xFF151122;
        secondaryColor = 0xFFFFAFA3;
        customBackground = false;
        useSecondary = false;
        opacity = 1f;
        blur = 0f;
        spacing = 1f;
        seed = 761;
        extra2 = 0xFF82B1FF;
        extra3 = 0xFFFFD180;
        pulse = false;
        extraCount = 1;
        photo = "";
        baseColor = 0xFF6750A4;
    }

    public static ShapeConfig load(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        ShapeConfig c = new ShapeConfig();
        c.useDynamicColors = p.getBoolean("dynamic", true);
        c.rotationSpeed = p.getFloat("speed", 1.4f);
        c.petalDepth = p.getFloat("petals", 0.15f);
        c.sizeMultiplier = p.getFloat("size", 0.30f);
        c.seed = p.getInt("seed", 761);
        c.extra2 = p.getInt("extra2", 0xFF82B1FF);
        c.extra3 = p.getInt("extra3", 0xFFFFD180);
        c.extraCount = p.getInt("extraCount", 1);
        c.pulse = p.getBoolean("pulse", false);
        c.photo = p.getString("photo", "");
        c.shapeMask = p.getInt("shape_mask", 127);
        c.backgroundColor = p.getInt("background_color", 0xFF151122);
        c.secondaryColor = p.getInt("secondary_color", 0xFFFFAFA3);
        c.customBackground = p.getBoolean("custom_bg", false);
        c.useSecondary = p.getBoolean("secondary", false);
        c.opacity = p.getFloat("opacity", 1f);
        c.blur = p.getFloat("blur", 0f);
        c.spacing = p.getFloat("spacing", 1f);
        c.baseColor = p.getInt("base_color", 0xFF6750A4);
        return c;
    }

    public void save(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt("seed", seed)
                .putInt("extra2", extra2)
                .putInt("extra3", extra3)
                .putInt("extraCount", extraCount)
                .putBoolean("pulse", pulse)
                .putString("photo", photo)
                .putInt("shape_mask", shapeMask)
                .putInt("background_color", backgroundColor)
                .putInt("secondary_color", secondaryColor)
                .putBoolean("custom_bg", customBackground)
                .putBoolean("secondary", useSecondary)
                .putFloat("opacity", opacity)
                .putFloat("blur", blur)
                .putFloat("spacing", spacing)
                .putBoolean("dynamic", useDynamicColors)
                .putFloat("speed", rotationSpeed)
                .putFloat("petals", petalDepth)
                .putFloat("size", sizeMultiplier)
                .putInt("base_color", baseColor)
                .apply();
    }
}
