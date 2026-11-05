package com.slimcorp.muilive;

import android.util.Base64;
import java.nio.ByteBuffer;

/** Versioned compact sharing code. Photo URIs are intentionally device-local. */
public final class SettingsCode {
    private static final int LENGTH = 52;
    public static String encode(ShapeConfig c) {
        ByteBuffer b=ByteBuffer.allocate(LENGTH);
        int flags=(c.useDynamicColors?1:0)|(c.customBackground?2:0)|(c.useSecondary?4:0)|(c.pulse?8:0);
        b.put((byte)1).put((byte)flags).put((byte)c.shapeMask).put((byte)c.extraCount);
        b.putInt(c.seed).putInt(c.baseColor).putInt(c.backgroundColor).putInt(c.secondaryColor).putInt(c.extra2).putInt(c.extra3);
        b.putFloat(c.rotationSpeed).putFloat(c.petalDepth).putFloat(c.sizeMultiplier).putFloat(c.opacity).putFloat(c.blur).putFloat(c.spacing);
        return "1"+Base64.encodeToString(b.array(),Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);
    }
    public static ShapeConfig decode(String code, ShapeConfig current) {
        if(code==null || !code.startsWith("1")) throw new IllegalArgumentException();
        byte[] raw=Base64.decode(code.substring(1).trim(),Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);
        if(raw.length!=LENGTH) throw new IllegalArgumentException();
        ByteBuffer b=ByteBuffer.wrap(raw); if(b.get()!=1) throw new IllegalArgumentException();
        int flags=b.get()&255, mask=b.get()&255, count=b.get()&255;
        if((flags & ~15)!=0 || mask<1 || mask>127 || count<1 || count>3) throw new IllegalArgumentException();
        ShapeConfig c=new ShapeConfig(); c.photo=current.photo;
        c.useDynamicColors=(flags&1)!=0; c.customBackground=(flags&2)!=0; c.useSecondary=(flags&4)!=0; c.pulse=(flags&8)!=0; c.shapeMask=mask; c.extraCount=count;
        c.seed=b.getInt(); c.baseColor=b.getInt(); c.backgroundColor=b.getInt(); c.secondaryColor=b.getInt(); c.extra2=b.getInt(); c.extra3=b.getInt();
        c.rotationSpeed=b.getFloat(); c.petalDepth=b.getFloat(); c.sizeMultiplier=b.getFloat(); c.opacity=b.getFloat(); c.blur=b.getFloat(); c.spacing=b.getFloat();
        if(!valid(c.rotationSpeed,.2f,10f)||!valid(c.petalDepth,0,.5f)||!valid(c.sizeMultiplier,.15f,.4f)||!valid(c.opacity,.1f,1f)||!valid(c.blur,0,1f)||!valid(c.spacing,.35f,1.5f)) throw new IllegalArgumentException();
        return c;
    }
    private static boolean valid(float v,float min,float max) { return Float.isFinite(v) && v>=min && v<=max; }
}
