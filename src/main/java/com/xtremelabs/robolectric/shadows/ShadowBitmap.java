package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Bitmap.class)
public class ShadowBitmap {
    @RealObject
    private Bitmap realBitmap;

    private int width;
    private int height;
    private String originalDescription = "Empty bitmap";
    private int loadedFromResourceId = -1;
    private List<DrawEvent> drawEvents = new ArrayList<DrawEvent>();

    @Implementation
    public boolean compress(Bitmap.CompressFormat format, int quality, OutputStream stream) {
        try {
            stream.write((originalDescription + " compressed as " + format + " with quality " + quality).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    @Implementation
    public static Bitmap createBitmap(int width, int height, Bitmap.Config config) {
        Bitmap scaledBitmap = Robolectric.newInstanceOf(Bitmap.class);
        ShadowBitmap shadowBitmap = shadowOf(scaledBitmap);
        shadowBitmap.appendDescription("Bitmap (" + width + " x " + height + ")");
        shadowBitmap.setWidth(width);
        shadowBitmap.setHeight(height);
        return scaledBitmap;
    }

    @Implementation
    public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
        Bitmap scaledBitmap = Robolectric.newInstanceOf(Bitmap.class);
        ShadowBitmap shadowBitmap = shadowOf(scaledBitmap);
        shadowBitmap.setOriginalDescription("Created from: " + shadowOf(src).getOriginalDescription());
        shadowBitmap.appendDescription(" scaled to " + dstWidth + " x " + dstHeight);
        if (filter) {
            shadowBitmap.appendDescription(" with filter " + filter);
        }
        shadowBitmap.setWidth(dstWidth);
        shadowBitmap.setHeight(dstHeight);
        return scaledBitmap;
    }

    private void appendDescription(String s) {
        originalDescription += s;
    }

    public void setOriginalDescription(String s) {
        originalDescription = s;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public static Bitmap create(String name) {
        Bitmap bitmap = Robolectric.newInstanceOf(Bitmap.class);
        shadowOf(bitmap).appendDescription(name);
        return bitmap;
    }

    public void setLoadedFromResourceId(int loadedFromResourceId) {
        this.loadedFromResourceId = loadedFromResourceId;
    }

    public int getLoadedFromResourceId() {
        if (loadedFromResourceId == -1) {
            throw new IllegalStateException("not loaded from a resource");
        }
        return loadedFromResourceId;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Implementation
    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Implementation
    public int getHeight() {
        return height;
    }

    @Override
    @Implementation
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != ShadowBitmap.class) return false;

        ShadowBitmap that = shadowOf((Bitmap) o);

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (originalDescription != null ? !originalDescription.equals(that.originalDescription) : that.originalDescription != null) return false;

        return true;
    }

    @Override
    @Implementation
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + (originalDescription != null ? originalDescription.hashCode() : 0);
        return result;
    }

    @Override
    @Implementation
    public String toString() {
        return "ShadowBitmap{" +
                "description='" + originalDescription + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

    public List<DrawEvent> getDrawEvents() {
        return Collections.unmodifiableList(drawEvents);
    }

    public void appendDrawEvent(DrawEvent drawEvent) {
        drawEvents.add(drawEvent);
    }

    public String getDescription() {
        return getDescription("");
    }

    private String getDescription(String indent) {
        String description = indent + originalDescription;
        for (DrawEvent drawEvent : drawEvents) {
            description += "\n\t" + indent + drawEvent.getCommand() + ": " + drawEvent.getDescription();
            ShadowBitmap bitmap = drawEvent.getBitmap();
            if (bitmap != null) {
                description += "\n\t\t" + indent + "with bitmap: \n" + bitmap.getDescription(indent + "\t\t\t");
            }
            String matrixDescription = drawEvent.getMatrixDescription();
            if (!matrixDescription.isEmpty()) {
                description += "\n\t\t" + indent + "with matrix: " + matrixDescription;
            }
            Paint drawEventPaint = drawEvent.getPaint();
            ColorFilter colorFilter = drawEventPaint != null ? drawEventPaint.getColorFilter() : null;
            if (colorFilter != null && colorFilter instanceof ColorMatrixColorFilter) {
                ShadowColorMatrixColorFilter filter = shadowOf((ColorMatrixColorFilter) colorFilter);
                description += "\n\t\t" + indent + "with color filter: " + filter.toString();
            }
        }
        return description;
    }

    public static class DrawEvent {
        private String command;
        private String description;
        private Paint paint;
        private ShadowBitmap bitmap;
        private ShadowMatrix matrix;

        public DrawEvent(String command, String description, Paint paint) {
            this(command, description, paint, null, null);
        }

        public DrawEvent(String command, String description, Paint paint, Bitmap bitmap) {
            this(command, description, paint, bitmap, null);
        }

        public DrawEvent(String command, String description, Paint paint, Matrix matrix) {
            this(command, description, paint, null, matrix);
        }

        public DrawEvent(String command, String description, Paint paint, Bitmap bitmap, Matrix matrix) {
            this.command = command;
            this.description = description;
            this.paint = paint;
            this.bitmap = bitmap == null ? null : shadowOf(bitmap);
            this.matrix = matrix == null ? null : shadowOf(matrix);
        }

        String getCommand() {
            return command;
        }

        public String getDescription() {
            return description;
        }

        public Paint getPaint() {
            return paint;
        }

        public ShadowBitmap getBitmap() {
            return bitmap;
        }

        public String getMatrixDescription() {
            return matrix == null ? "" : matrix.getDescription();
        }
    }
}
