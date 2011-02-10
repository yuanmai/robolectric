package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class CanvasTest {
    private Bitmap targetBitmap;
    private Canvas canvas;
    private Paint paint;
    private Bitmap imageBitmap;

    @Before
    public void setUp() throws Exception {
        targetBitmap = Robolectric.newInstanceOf(Bitmap.class);
        canvas = new Canvas(targetBitmap);
        paint = new Paint();
        imageBitmap = BitmapFactory.decodeFile("/an/image.jpg");
    }

    @Test
    public void getBitmap_shouldReturnTheBitmapSpecifiedInTheCtor() throws Exception {
        assertThat(shadowOf(canvas).getBitmap(), equalTo(targetBitmap));
    }

    @Test
    public void getDrawEvents_shouldReturnTheDrawingEventsFromTheAssociatedBitmap() throws Exception {
        ShadowCanvas shadowCanvas = shadowOf(canvas);
        ShadowBitmap shadowBitmap = shadowOf(shadowCanvas.getBitmap());
        assertThat(shadowCanvas.getDrawEvents(), equalTo(shadowBitmap.getDrawEvents()));
    }

    @Test
    public void getDrawEvents_whenNothingHasBeenDrawn_shouldBeEmpty() throws Exception {
        assertThat(shadowOf(canvas).getDrawEvents().size(), equalTo(0));
    }

    @Test
    public void shouldDrawCircles() throws Exception {
        canvas.drawCircle(10.1f, 20, 30, paint);
        checkDrawEvent("drawCircle", "cx: 10.1, cy: 20.0, radius: 30.0");
    }

    @Test
    public void shouldDrawPaint() throws Exception {
        canvas.drawPaint(paint);
        checkDrawEvent("drawPaint", "");
    }

    @Test
    public void shouldDrawBitmaps() throws Exception {
        Bitmap imageBitmap = BitmapFactory.decodeFile("/an/image.jpg");

        canvas.drawBitmap(imageBitmap, 10.5f, 15.7f, paint);
        checkDrawEvent("drawBitmap", "left: 10.5, top: 15.7");
        String bitmapDescription = shadowOf(canvas).getDescription();
        assertThat(bitmapDescription, containsString("Bitmap for file: /an/image.jpg"));
        assertThat(bitmapDescription, not(containsString("with matrix")));
    }

    @Test
    public void shouldDrawBitmapsWithAMatrix() throws Exception {
        Bitmap imageBitmap = BitmapFactory.decodeFile("/an/image.jpg");
        Matrix matrix = new Matrix();
        matrix.setTranslate(15, 20);

        canvas.drawBitmap(imageBitmap, matrix, paint);

        checkDrawEvent("drawBitmap", "left: 0.0, top: 0.0");
        String bitmapDescription = shadowOf(canvas).getDescription();
        assertThat(bitmapDescription, containsString("Bitmap for file: /an/image.jpg"));
        assertThat(bitmapDescription, containsString("translateX: 15.0, translateY: 20.0"));

    }

    public void checkDrawEvent(String commandName, String description) {
        List<ShadowBitmap.DrawEvent> drawingEvents = shadowOf(canvas).getDrawEvents();
        assertThat(drawingEvents.size(), equalTo(1));

        ShadowBitmap.DrawEvent drawEvent = drawingEvents.get(0);
        assertThat(drawEvent.getCommand(), equalTo(commandName));
        assertThat(drawEvent.getDescription(), containsString(description));
        assertThat(drawEvent.getPaint(), equalTo(paint));
    }




    @Test
    public void shouldCreateScaledBitmap() throws Exception {
        Bitmap originalBitmap = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(originalBitmap).setOriginalDescription("Original bitmap");
        
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 200, false);

        assertEquals("Created from: Original bitmap scaled to 100 x 200", shadowOf(scaledBitmap).getDescription());
        assertEquals(100, scaledBitmap.getWidth());
        assertEquals(200, scaledBitmap.getHeight());
    }

    @Test
    public void shouldReceiveDescriptionWhenDrawingToCanvas() throws Exception {
        Bitmap bitmap1 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap1).setOriginalDescription("Bitmap One");

        Bitmap bitmap2 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap2).setOriginalDescription("Bitmap Two");

        Canvas canvas = new Canvas(bitmap1);
        canvas.drawBitmap(bitmap2, 0, 0, null);

        assertTrue(shadowOf(bitmap1).getDescription().startsWith("Bitmap One"));
        assertTrue(shadowOf(bitmap1).getDescription().contains("with bitmap:"));
        assertTrue(shadowOf(bitmap1).getDescription().endsWith("Bitmap Two"));
    }

    @Test
    public void shouldReceiveEventsWhenDrawingToCanvas() throws Exception {
        Bitmap bitmap1 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap1).setOriginalDescription("Bitmap One");

        Bitmap bitmap2 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap2).setOriginalDescription("Bitmap Two");

        Canvas canvas = new Canvas(bitmap1);
        canvas.drawBitmap(bitmap2, 0, 0, null);

        assertTrue(shadowOf(bitmap1).getDrawEvents().createdFrom("Bitmap One").isTrue());
        assertFalse(shadowOf(bitmap1).getDrawEvents().createdFrom("Bitmap Two").isTrue());
        assertTrue(shadowOf(bitmap1).getDrawEvents().createdFrom("Bitmap One").has("drawBitmap").isTrue());
        assertTrue(shadowOf(bitmap1).getDrawEvents().createdFrom("Bitmap One").has("drawBitmap").with("left: 0.0, top: 0.0").isTrue());
        assertTrue(shadowOf(bitmap1).getDrawEvents().createdFrom("Bitmap One").has("drawBitmap").withBitmap().isTrue());
//        ShadowBitmap.DrawEventQuery query = shadowOf(bitmap1).getDrawEvents().has(bitmap2).with("drawBitmap").followedBy();
//        query.hasMatrix();
//        query.hasBitmap().createdFrom("Bitmap One");
    }

    @Test
    public void queries_shouldAllApplyToTheSameEvent() throws Exception {
        Bitmap bitmap1 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap1).setOriginalDescription("Bitmap One");

        Bitmap bitmap2 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap2).setOriginalDescription("Bitmap Two");

        Bitmap bitmap3 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap3).setOriginalDescription("Bitmap Three");

        Canvas canvas = new Canvas(bitmap1);
        canvas.drawBitmap(bitmap2, 0, 0, null);
        canvas.drawBitmap(bitmap3, 1, 1, null);

        assertTrue(shadowOf(bitmap1).getDrawEvents().createdFrom("Bitmap One").has("drawBitmap").with("left: 0.0, top: 0.0").withBitmap().createdFrom("Bitmap Two").isTrue());
        assertFalse(shadowOf(bitmap1).getDrawEvents().createdFrom("Bitmap One").has("drawBitmap").with("left: 0.0, top: 0.0").withBitmap().createdFrom("Bitmap Three").isTrue());
    }

    @Test
    public void shouldReceiveDescriptionWhenDrawingToCanvasWithBitmapAndMatrixAndPaint() throws Exception {
        Bitmap bitmap1 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap1).setOriginalDescription("Bitmap One");

        Bitmap bitmap2 = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap2).setOriginalDescription("Bitmap Two");

        Canvas canvas = new Canvas(bitmap1);
        canvas.drawBitmap(bitmap2, new Matrix(), null);

        String description = shadowOf(canvas).getDescription();
        assertTrue(description, description.startsWith("Bitmap One"));
        assertTrue(description, description.contains("with bitmap:"));
        assertTrue(description, description.contains("Bitmap Two"));
        assertTrue(description, description.contains("with matrix:"));
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

        String description = shadowOf(canvas).getDescription();
        assertTrue(description, description.startsWith("Bitmap One"));
        assertTrue(description, description.contains("with bitmap:"));
        assertTrue(description, description.contains("Bitmap Two"));
        assertTrue(description, description.contains("with matrix:"));
        assertTrue(description, description.contains("with color filter:"));
        assertTrue(description, description.contains("with matrix:"));
        assertTrue(description, description.contains("ColorMatrixColorFilter<1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0>"));
    }

    @Test
    public void visualize_shouldReturnDescription() throws Exception {
        Bitmap bitmap = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap).setOriginalDescription("Bitmap One");


        assertEquals("Bitmap One", Robolectric.visualize(bitmap));
    }

    @Test
    public void shouldDescribeBitmapDrawing() throws Exception {
        Canvas canvas = new Canvas(targetBitmap);
        canvas.drawBitmap(imageBitmap, 1, 2, new Paint());
        canvas.drawBitmap(imageBitmap, 100, 200, new Paint());

        String description = shadowOf(canvas).getDescription();
        assertTrue(description, description.contains("Bitmap for file: /an/image.jpg"));
        description.replace("Bitmap for file: /an/image.jpg", "");
        assertTrue(description, description.contains("Bitmap for file: /an/image.jpg"));
        assertTrue(description, description.contains("left: 1.0, top: 2.0"));
        assertTrue(description, description.contains("left: 100.0, top: 200.0"));
    }

    @Test
    public void shouldDescribeBitmapDrawing_WithMatrix() throws Exception {
        Canvas canvas = new Canvas(targetBitmap);
        canvas.drawBitmap(imageBitmap, new Matrix(), new Paint());
        canvas.drawBitmap(imageBitmap, new Matrix(), new Paint());

        String description = shadowOf(canvas).getDescription();
        assertHasTwoBitmapsWithMatrix(description);

        description = shadowOf(targetBitmap).getDescription();
        assertHasTwoBitmapsWithMatrix(description);
    }

    private void assertHasTwoBitmapsWithMatrix(String description) {
        assertTrue(description, description.contains("Bitmap for file: /an/image.jpg"));
        assertTrue(description, description.contains("with matrix:"));
        description.replace("Bitmap for file: /an/image.jpg", "");
        description.replace("with matrix:", "");
        assertTrue(description, description.contains("Bitmap for file: /an/image.jpg"));
        assertTrue(description, description.contains("with matrix:"));
    }

    @Test
    public void shouldRescaleBitmap() throws Exception {
        Bitmap originalBitmap = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(originalBitmap).setOriginalDescription("Original bitmap");

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 200, false);
        Canvas scaledBitmapCanvas = new Canvas(scaledBitmap);
        scaledBitmapCanvas.scale(1.5f, 1.5f);

        String description = shadowOf(scaledBitmapCanvas).getDescription();
        assertTrue(description, description.contains("Created from: Original bitmap scaled to 100 x 200"));
        assertTrue(description, description.contains("scale: by: 1.5 x 1.5"));

        assertEquals(150, scaledBitmap.getWidth());
        assertEquals(300, scaledBitmap.getHeight());
    }
}
