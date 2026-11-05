package com.slimcorp.muilive;
import static org.junit.Assert.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
@RunWith(AndroidJUnit4.class)
public class SettingsCodeTest {
    @Test public void roundTripAndRejectCorrupt() {
        ShapeConfig c=new ShapeConfig(); c.seed=-123456; c.shapeMask=5; c.extraCount=3; c.extra2=0xff123456; c.pulse=true; c.photo="content://local/photo"; c.spacing=.4f;
        String code=SettingsCode.encode(c);
        ShapeConfig existing=new ShapeConfig(); existing.photo="content://recipient/photo";
        ShapeConfig decoded=SettingsCode.decode(code,existing);
        assertEquals(c.seed,decoded.seed); assertEquals(c.shapeMask,decoded.shapeMask); assertEquals(c.extra2,decoded.extra2); assertTrue(decoded.pulse); assertEquals(c.spacing,decoded.spacing,0f); assertEquals(existing.photo,decoded.photo);
        try { SettingsCode.decode("1bad",existing); fail("Invalid code accepted"); } catch(IllegalArgumentException expected) { }
    }
    @Test public void qrRoundTrip() throws Exception {
        ShapeConfig c=new ShapeConfig(); c.seed=123456; c.shapeMask=19; String encoded=SettingsCode.encode(c);
        android.graphics.Bitmap image=new com.journeyapps.barcodescanner.BarcodeEncoder().encodeBitmap(encoded,com.google.zxing.BarcodeFormat.QR_CODE,500,500);
        int[] pixels=new int[500*500]; image.getPixels(pixels,0,500,0,0,500,500);
        com.google.zxing.RGBLuminanceSource source=new com.google.zxing.RGBLuminanceSource(500,500,pixels);
        String scanned=new com.google.zxing.MultiFormatReader().decode(new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source))).getText();
        ShapeConfig restored=SettingsCode.decode(scanned,new ShapeConfig()); assertEquals(c.seed,restored.seed); assertEquals(c.shapeMask,restored.shapeMask);
    }
}
