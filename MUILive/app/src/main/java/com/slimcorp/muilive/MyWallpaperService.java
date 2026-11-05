package com.slimcorp.muilive;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.List;

public class MyWallpaperService extends WallpaperService {
    private final List<MaterialEngine> engines = new ArrayList<>();

    @Override public Engine onCreateEngine() {
        MaterialEngine engine = new MaterialEngine();
        engines.add(engine);
        return engine;
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        for (MaterialEngine engine : engines) engine.scheduleRebuild();
    }

    private final class MaterialEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final WallpaperRenderer renderer = new WallpaperRenderer(MyWallpaperService.this);
        private boolean visible;
        private final Runnable drawRunner = this::drawFrame;
        private final Runnable rebuildRunner = () -> {
            renderer.setConfig(ShapeConfig.load(MyWallpaperService.this));
            if (visible) drawFrame();
        };
        private SharedPreferences preferences;
        private int width, height;
        private long lastFrameTime;
        private float elapsed, currentOffset = .5f, targetOffset = .5f;

        @Override public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            preferences = getSharedPreferences("material_shapes_prefs", MODE_PRIVATE);
            preferences.registerOnSharedPreferenceChangeListener(this);
            renderer.setConfig(ShapeConfig.load(MyWallpaperService.this));
        }
        @Override public void onSharedPreferenceChanged(SharedPreferences prefs, String key) { scheduleRebuild(); }
        private void scheduleRebuild() { handler.removeCallbacks(rebuildRunner); handler.postDelayed(rebuildRunner, 60); }
        @Override public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            this.width = width; this.height = height;
            renderer.resize(width, height);
            lastFrameTime = SystemClock.elapsedRealtime();
            if (visible) drawFrame();
        }
        @Override public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            targetOffset = xOffset;
        }
        @Override public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            handler.removeCallbacks(drawRunner);
            if (visible) { lastFrameTime = SystemClock.elapsedRealtime(); drawFrame(); }
        }
        @Override public void onSurfaceDestroyed(SurfaceHolder holder) {
            handler.removeCallbacks(drawRunner);
            width = height = 0;
            super.onSurfaceDestroyed(holder);
        }
        @Override public void onDestroy() {
            visible = false;
            handler.removeCallbacks(drawRunner); handler.removeCallbacks(rebuildRunner);
            if (preferences != null) preferences.unregisterOnSharedPreferenceChangeListener(this);
            engines.remove(this);
            super.onDestroy();
        }
        private void drawFrame() {
            handler.removeCallbacks(drawRunner);
            if (!visible || width <= 0 || height <= 0) return;
            Canvas canvas = null;
            SurfaceHolder holder = getSurfaceHolder();
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    long now = SystemClock.elapsedRealtime();
                    float dt = Math.min(.1f, Math.max(0f, (now - lastFrameTime) / 1000f));
                    lastFrameTime = now; elapsed += dt;
                    currentOffset += (targetOffset - currentOffset) * Math.min(1f, 10f * dt);
                    renderer.draw(canvas, elapsed, currentOffset);
                }
            } finally { if (canvas != null) holder.unlockCanvasAndPost(canvas); }
            if (visible) handler.postDelayed(drawRunner, 33);
        }
    }
}
