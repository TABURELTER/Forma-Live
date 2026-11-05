package com.slimcorp.muilive;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.Random;

/** Procedural Material-inspired composition; no bitmap artwork. */
public final class WallpaperRenderer {
    private final Context context;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path[] outlines = new Path[7];
    private ShapeConfig config;
    private android.graphics.BlurMaskFilter blurFilter;
    private int width, height, background;
    private android.graphics.Bitmap photo; private String photoUri="";
    private final int[] palette = new int[6];
    public WallpaperRenderer(Context context) { this.context = context.getApplicationContext(); }
    public void setConfig(ShapeConfig config) {
        this.config = config;
        if (!config.photo.equals(photoUri)) { photoUri=config.photo; photo=null; if (!photoUri.isEmpty()) try (java.io.InputStream in=context.getContentResolver().openInputStream(android.net.Uri.parse(photoUri))) { android.graphics.BitmapFactory.Options o=new android.graphics.BitmapFactory.Options(); o.inSampleSize=2; photo=android.graphics.BitmapFactory.decodeStream(in,null,o); } catch (Exception ignored) {} }
        blurFilter = config.blur > 0 ? new android.graphics.BlurMaskFilter(config.blur * .03f, android.graphics.BlurMaskFilter.Blur.NORMAL) : null;
        int base = config.useDynamicColors ? context.getColor(android.R.color.system_accent1_500) : config.baseColor;
        float[] hsv = new float[3]; Color.colorToHSV(base, hsv);
        background = config.customBackground ? config.backgroundColor : tone(hsv[0], .30f, .10f);
        for (int i = 0; i < palette.length; i++) palette[i] = tone(hsv[0] + (i-2)*9, .20f + i*.045f, .99f - i*.055f);
        for (int k=0;k<outlines.length;k++) outlines[k]=createShape(k, config.petalDepth);
    }
    private int tone(float h,float s,float v) { return Color.HSVToColor(new float[]{(h+360)%360,s,v}); }
    public void resize(int w,int h) { width=w; height=h; }
    private Path createShape(int kind, float depth) {
        Path p=new Path(); int n=240;
        for(int i=0;i<=n;i++) {
            double a=2*Math.PI*i/n; float c=(float)Math.cos(a), s=(float)Math.sin(a), x,y;
            if(kind==0) { x=c; y=s; }
            else if(kind==1) { x=Math.signum(c)*(float)Math.pow(Math.abs(c),.55); y=Math.signum(s)*(float)Math.pow(Math.abs(s),.55); }
            else if(kind==2 || kind==3) { float r=1-(.08f+depth*.5f)*(1-(float)Math.cos((kind==2?8:5)*a))*.5f; x=c*r; y=s*r; }
            else if(kind==5) { float r=1-(.17f+depth*.35f)*(1-(float)Math.cos(12*a))*.5f; x=c*r; y=s*r; }
            else if(kind==6) { x=c*(.72f+.28f*Math.abs(s)); y=s; }
            else { float r=(float)(Math.cos(Math.PI/6)/Math.cos((a+Math.PI/6)%(Math.PI/3)-Math.PI/6)); x=c*r; y=s*r; }
            if(i==0)p.moveTo(x,y);else p.lineTo(x,y);
        }
        p.close(); return p;
    }
    public void draw(Canvas canvas,float elapsed,float offset) {
        if(config==null || width<=0 || height<=0)return;
        canvas.drawColor(background);
        if(photo!=null) { float scale=Math.max(width/(float)photo.getWidth(),height/(float)photo.getHeight()); float w=photo.getWidth()*scale,h=photo.getHeight()*scale; paint.setAlpha(255); paint.setMaskFilter(null); canvas.drawBitmap(photo,null,new android.graphics.RectF((width-w)/2,(height-h)/2,(width+w)/2,(height+h)/2),paint); }
        float cell=width*.46f*config.spacing, radius=cell*(.26f+config.sizeMultiplier*.5f);
        int rows=(int)Math.ceil(height/cell)+3;
        float parallax=(offset-.5f)*cell*.65f;
        Random random=new Random(config.seed);
        for(int row=-1;row<rows;row++) for(int col=-1;col<(int)Math.ceil(width/cell)+2;col++) {
            int kind=random.nextInt(outlines.length), color=random.nextInt(palette.length);
            for (int j=0; j<outlines.length && (config.shapeMask & (1<<kind))==0; j++) kind=(kind+1)%outlines.length;
            float phase=random.nextFloat()*6.28f, direction=random.nextBoolean()?1:-1;
            float x=(col+.5f+(row%2==0?.5f:0))*cell-parallax;
            float y=(row+.5f)*cell;
            float speed=elapsed*config.rotationSpeed;
            float pulse=1+(config.pulse?.035f:0f)*(float)Math.sin(speed*.55f+phase);
            canvas.save(); canvas.translate(x,y); canvas.rotate(direction*speed*3+phase*57); canvas.scale(radius*pulse,radius*pulse);
            paint.setColor(config.useSecondary && color < config.extraCount ? (color==0?config.secondaryColor:color==1?config.extra2:config.extra3) : palette[color]); paint.setAlpha(Math.round(config.opacity*255)); paint.setStyle(Paint.Style.FILL);
            paint.setMaskFilter(blurFilter);
            canvas.drawPath(outlines[kind],paint); paint.setMaskFilter(null);
            canvas.restore();
        }
    }
}

