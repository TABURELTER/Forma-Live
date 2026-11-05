package com.slimcorp.muilive;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/** The same renderer used by the live wallpaper, shown inside the settings and fullscreen screens. */
public class WallpaperPreviewView extends View {
    private final WallpaperRenderer renderer;
    private final Paint scrim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private long lastFrame;
    private float elapsed;
    private int artworkWidth, artworkHeight;
    private float currentOffset = 0.5f;
    private float targetOffset = 0.5f;
    private float lastTouchX;
    private boolean isInteractive = true;
    private boolean showScrim = true;

    public WallpaperPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        renderer = new WallpaperRenderer(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setConfig(ShapeConfig config) {
        renderer.setConfig(config);
        invalidate();
    }

    public void setShowScrim(boolean show) {
        this.showScrim = show;
        invalidate();
    }

    public void setInteractive(boolean interactive) {
        this.isInteractive = interactive;
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        artworkWidth = metrics.widthPixels;
        artworkHeight = metrics.heightPixels;
        renderer.resize(artworkWidth, artworkHeight);
        scrim.setShader(new LinearGradient(0, h * .50f, 0, h,
                Color.TRANSPARENT, 0xD00D0E15, Shader.TileMode.CLAMP));
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (!isInteractive) return super.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchX;
                lastTouchX = event.getX();
                targetOffset = Math.max(0f, Math.min(1f, targetOffset - (dx / (float) Math.max(1, getWidth()))));
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = SystemClock.elapsedRealtime();
        if (lastFrame != 0) {
            float dt = Math.min(.1f, (now - lastFrame) / 1000f);
            elapsed += dt;
            currentOffset += (targetOffset - currentOffset) * Math.min(1f, 12f * dt);
        }
        lastFrame = now;
        float scale = Math.max(getWidth() / (float) artworkWidth, getHeight() / (float) artworkHeight);
        canvas.save();
        canvas.translate((getWidth() - artworkWidth * scale) / 2f, (getHeight() - artworkHeight * scale) / 2f);
        canvas.scale(scale, scale);
        renderer.draw(canvas, elapsed, currentOffset);
        canvas.restore();
        if (showScrim) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), scrim);
        }
        if (isShown()) postInvalidateDelayed(33);
    }

    @Override protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        lastFrame = 0;
    }
}
