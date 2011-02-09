package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadows the {@code android.graphics.Canvas} class.
 * <p/>
 * Broken.
 * This implementation is very specific to the application for which it was developed.
 * Todo: Reimplement. Consider using the same strategy of collecting a history of draw events and providing methods for writing queries based on type, number, and order of events.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Canvas.class)
public class ShadowCanvas {
    private Bitmap targetBitmap = newInstanceOf(Bitmap.class);

    private float translateX;
    private float translateY;
//    private float scaleX = 1;
//    private float scaleY = 1;

    public void __constructor__(Bitmap bitmap) {
        this.targetBitmap = bitmap;
    }

    public String getDescription() {
        return shadowOf(targetBitmap).getDescription();
    }

    @Implementation
    public void translate(float x, float y) {
        this.translateX = x;
        this.translateY = y;
    }

    @Implementation
    public void scale(float sx, float sy) {
//        this.scaleX = sx;
//        this.scaleY = sy;
    }

    @Implementation
    public void scale(float sx, float sy, float px, float py) {
//        this.scaleX = sx;
//        this.scaleY = sy;
    }

    @Implementation
    public void drawPaint(Paint paint) {
        appendDrawEvent(new ShadowBitmap.DrawEvent("drawPaint", "", paint));
    }

    @Implementation
    public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        internalDrawBitmap(bitmap, left, top, paint, null);
    }

    @Implementation
    public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        internalDrawBitmap(bitmap, 0, 0, paint, matrix);
    }

    private void internalDrawBitmap(Bitmap bitmap, float left, float top, Paint paint, Matrix matrix) {
        appendDrawEvent(new ShadowBitmap.DrawEvent(
                "drawBitmap",
                "left: " + (translateX + left) + ", top: " + (translateY + top),
                paint,
                bitmap,
                matrix));
    }

    @Implementation
    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        appendDrawEvent(new ShadowBitmap.DrawEvent("drawCircle", "cx: " + cx + ", cy: " + cy + ", radius: " + radius, paint));
    }

//    private void describeBitmap(Bitmap bitmap, Paint paint) {
//        if (getDescription().length() != 0) {
//            appendDescription("\n");
//        }
//
//        appendDescription(shadowOf(bitmap).getDescription());
//
//        if (paint != null) {
//            ColorFilter colorFilter = paint.getColorFilter();
//            if (colorFilter != null) {
//                appendDescription(" with " + colorFilter);
//            }
//        }
//    }

    public List<ShadowBitmap.DrawEvent> getDrawEvents() {
        return shadowOf(targetBitmap).getDrawEvents();
    }

    public Bitmap getBitmap() {
        return targetBitmap;
    }


    private void appendDrawEvent(ShadowBitmap.DrawEvent event) {
        shadowOf(targetBitmap).appendDrawEvent(event);
    }

}
