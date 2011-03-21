package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.containsString;
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
        assertThat(shadowOf(emptyBitmap).getOriginalDescription(), equalTo("Empty bitmap"));
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
    @Ignore
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

    @Test
    public void shouldReceiveDescriptionWhenDrawingToCanvas() throws Exception {
        Bitmap bitmap1 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap1).setOriginalDescription("Bitmap One");

        Bitmap bitmap2 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap2).setOriginalDescription("Bitmap Two");

        Canvas canvas = new Canvas(bitmap1);
        canvas.drawBitmap(bitmap2, 0, 0, null);

        assertThat(shadowOf(bitmap1).getDescription(), containsString("Bitmap One"));
        assertThat(shadowOf(bitmap1).getDescription(), containsString("Bitmap Two"));
    }

    @Test
    public void shouldReceiveDescriptionWhenDrawingToCanvasWithBitmapAndMatrixAndPaint() throws Exception {
        Bitmap bitmap1 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap1).setOriginalDescription("Bitmap One");

        Bitmap bitmap2 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap2).setOriginalDescription("Bitmap Two");

        Canvas canvas = new Canvas(bitmap1);
        canvas.drawBitmap(bitmap2, new Matrix(), null);

        final String description = shadowOf(bitmap1).getDescription();
        assertThat(description, containsString("Bitmap One"));
        assertThat(description, containsString("Bitmap Two"));
        assertThat(description, containsString("with matrix"));
    }

    @Test
    public void shouldReceiveDescriptionWhenDrawABitmapToCanvasWithAPaintEffect() throws Exception {
        Bitmap bitmap1 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap1).setOriginalDescription("Bitmap One");

        Bitmap bitmap2 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap2).setOriginalDescription("Bitmap Two");

        Canvas canvas = new Canvas(bitmap1);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
        canvas.drawBitmap(bitmap2, new Matrix(), paint);

        final String description = shadowOf(bitmap1).getDescription();
        assertThat(description, containsString("Bitmap One"));
        assertThat(description, containsString("Bitmap Two"));
        assertThat(description, containsString("with matrix"));
        assertThat(description, containsString("with color filter: ColorMatrixColorFilter<1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0>"));
    }

    @Test
    public void visualize_shouldReturnDescription() throws Exception {
        Bitmap bitmap = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap).setOriginalDescription("Bitmap One");

        assertEquals("Bitmap One", Robolectric.visualize(bitmap));

    }

    @Test
    public void shouldCopyBitmap() {
        Bitmap bitmap = Robolectric.newInstanceOf(Bitmap.class);
        Bitmap bitmapCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        assertEquals(shadowOf(bitmapCopy).getConfig(), Bitmap.Config.ARGB_8888);
        assertTrue(shadowOf(bitmapCopy).isMutable());
    }
}
