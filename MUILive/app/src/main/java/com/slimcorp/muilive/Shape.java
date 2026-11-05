package com.slimcorp.muilive;

import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;

/** An immutable local-space outline, reused for every animation frame. */
public class Shape {
    public enum Kind { CIRCLE, SQUIRCLE, HEXAGON, FLOWER }

    private final float cx, cy, rotationSpeed;
    private float rotation;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    public Shape(Kind kind, int color, float radius, float cx, float cy,
                 float rotationSpeed, float petals, float petalDepth) {
        this.cx = cx;
        this.cy = cy;
        this.rotationSpeed = rotationSpeed;
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setPathEffect(new CornerPathEffect(radius * 0.2f));
        buildPath(kind, radius, petals, petalDepth);
    }

    public void update(float dt) {
        rotation = (rotation + rotationSpeed * dt) % 360f;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(cx * canvas.getWidth(), cy * canvas.getHeight());
        canvas.rotate(rotation);
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    private void buildPath(Kind kind, float r, float petals, float petalDepth) {
        if (kind == Kind.CIRCLE) {
            path.addCircle(0, 0, r, Path.Direction.CW);
            return;
        }
        int steps = kind == Kind.HEXAGON ? 6 : kind == Kind.SQUIRCLE ? 96 : 180;
        for (int i = 0; i < steps; i++) {
            double angle = 2 * Math.PI * i / steps;
            if (kind == Kind.HEXAGON) angle += Math.PI / 6;
            double cos = Math.cos(angle), sin = Math.sin(angle);
            float x, y;
            if (kind == Kind.SQUIRCLE) {
                x = r * Math.signum((float) cos) * (float) Math.sqrt(Math.abs(cos));
                y = r * Math.signum((float) sin) * (float) Math.sqrt(Math.abs(sin));
            } else {
                float currentRadius = kind == Kind.FLOWER
                        ? r * (1 + petalDepth * (float) Math.cos(angle * petals)) : r;
                x = currentRadius * (float) cos;
                y = currentRadius * (float) sin;
            }
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        path.close();
    }
}
