package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Paint;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class BitmapTest {
    @Test
    public void defaultCtor_shouldSetTheOriginToDefault() throws Exception {
        Bitmap emptyBitmap = Robolectric.newInstanceOf(Bitmap.class);
        assertThat(shadowOf(emptyBitmap).getOrigin(), equalTo("Empty bitmap"));
    }

    @Test
    public void shouldAllowAppendingDrawEvents() throws Exception {
        ShadowBitmap shadowBitmap = shadowOf(Robolectric.newInstanceOf(Bitmap.class));
        Paint paint = new Paint();
        shadowBitmap.appendDrawEvent(new ShadowBitmap.DrawEvent("command", "description", paint));

        ShadowBitmap.DrawEvent drawEvent = shadowBitmap.getDrawEvents().get(0);
        assertEquals("command", drawEvent.getCommand());
        assertEquals("description", drawEvent.getDescription());
        assertEquals(paint, drawEvent.getPaint());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldEncapsulateDrawEvents() throws Exception {
        ShadowBitmap shadowBitmap = shadowOf(Robolectric.newInstanceOf(Bitmap.class));
        List<ShadowBitmap.DrawEvent> drawEvents = shadowBitmap.getDrawEvents();
        drawEvents.add(null);
    }
    
    @Test
    public void equals_shouldCompareDescriptions() throws Exception {
        assertFalse(ShadowBitmap.create("bitmap A").equals(ShadowBitmap.create("bitmap B")));

        assertTrue(ShadowBitmap.create("bitmap A").equals(ShadowBitmap.create("bitmap A")));
    }

    @Test
    public void equals_shouldCompareWidthAndHeight() throws Exception {
        Bitmap bitmapA1 = ShadowBitmap.create("bitmap A");
        shadowOf(bitmapA1).setWidth(100);
        shadowOf(bitmapA1).setHeight(100);

        Bitmap bitmapA2 = ShadowBitmap.create("bitmap A");
        shadowOf(bitmapA2).setWidth(101);
        shadowOf(bitmapA2).setHeight(101);

        assertFalse(bitmapA1.equals(bitmapA2));
    }

}
