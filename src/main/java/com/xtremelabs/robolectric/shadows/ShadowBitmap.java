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

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Bitmap.class)
public class ShadowBitmap {
    @RealObject
    private Bitmap realBitmap;

    private int width;
    private int height;
    private Bitmap.Config config;
    private boolean mutable;
    private String description = "";
    private String originalDescription = "Empty bitmap";
    private int loadedFromResourceId = -1;
    private DrawEvents drawEvents = new DrawEvents();
    private boolean recycled = false;

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
        shadowBitmap.setConfig(config);
        return scaledBitmap;
    }
    
    @Implementation
    public static Bitmap createBitmap(Bitmap bitmap) {
        ShadowBitmap shadowBitmap = shadowOf(bitmap);
        shadowBitmap.appendDescription(" created from Bitmap object");
        return bitmap;   	
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
    
    @Implementation
    public void recycle() {
    	recycled = true;
    }

    @Implementation
    public final boolean isRecycled() {
        return recycled;
    }

    @Implementation
    public Bitmap copy(Bitmap.Config config, boolean isMutable) {
        ShadowBitmap shadowBitmap = shadowOf(realBitmap);
        shadowBitmap.setConfig(config);
        shadowBitmap.setMutable(isMutable);
        return realBitmap;
    }

    @Implementation
    public final Bitmap.Config getConfig() {
        return config;
    }

    public void setConfig(Bitmap.Config config) {
        this.config = config;
    }

    @Implementation
    public final boolean isMutable() {
        return mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
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

    public void scale(float sx, float sy) {
        setHeight((int) (getHeight() * sx));
        setWidth((int) (getWidth() * sy));
        appendDrawEvent(new ShadowBitmap.DrawEvent("scale", "by: " + sx + " x " + sy));
    }

    @Override
    @Implementation
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != ShadowBitmap.class) return false;

        ShadowBitmap that = shadowOf((Bitmap) o);

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (originalDescription != null ? !originalDescription.equals(that.originalDescription) : that.originalDescription != null)
            return false;

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

    public Bitmap getRealBitmap() {
        return realBitmap;
    }

    public DrawEvents getDrawEvents() {
        return drawEvents;
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

    public class DrawEvents extends ArrayList<DrawEvent> {
        public DrawEventQuery createdFrom(String expectedDescription) {
            return new DefaultQuery(originalDescription.equals(expectedDescription));
        }
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

        public DrawEvent(String command, String description) {
            this(command, description, null, null, null);
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

    public abstract class DrawEventQuery {
        DrawEventQuery parent;

        public DrawEventQuery(DrawEventQuery parent) {
            this.parent = parent;
        }

        public boolean isTrue() {
            for (DrawEvent drawEvent : drawEvents) {
                if (internalIsTrueFor(drawEvent)) {
                    return true;
                }
            }
            return false;
        }

        private boolean internalIsTrueFor(DrawEvent drawEvent) {
            return (parent == null || parent.internalIsTrueFor(drawEvent)) && isTrueFor(drawEvent);
        }

        protected abstract boolean isTrueFor(DrawEvent drawEvent);

        public DrawEventQuery has(String command) {
            return new HasQuery(this, command);
        }

        public DrawEventQuery with(String description) {
            return new WithQuery(this, description);
        }

        public DrawEventQuery withBitmap() {
            return new WithBitmapQuery(this);
        }

        public DrawEventQuery createdFrom(String bitmapName) {
            return new CreatedFromQuery(this, bitmapName);
        }
    }

    public class HasQuery extends DrawEventQuery {
        String command;

        public HasQuery(DrawEventQuery parent, String command) {
            super(parent);
            this.command = command;
        }

        @Override protected boolean isTrueFor(DrawEvent drawEvent) {
            return drawEvent.getCommand().equals(command);
        }
    }

    public class WithQuery extends DrawEventQuery {
        String description;

        public WithQuery(DrawEventQuery parent, String description) {
            super(parent);
            this.description = description;
        }

        @Override protected boolean isTrueFor(DrawEvent drawEvent) {
            return drawEvent.getDescription().equals(description);
        }
    }

    public class WithBitmapQuery extends DrawEventQuery {
        public WithBitmapQuery(DrawEventQuery parent) {
            super(parent);
        }

        @Override protected boolean isTrueFor(DrawEvent drawEvent) {
            return drawEvent.getBitmap() != null;
        }
    }

    public class CreatedFromQuery extends DrawEventQuery {
        String bitmapName;

        public CreatedFromQuery(DrawEventQuery parent, String bitmapName) {
            super(parent);
            this.bitmapName = bitmapName;
        }

        @Override protected boolean isTrueFor(DrawEvent drawEvent) {
            return drawEvent.getBitmap() != null && drawEvent.getBitmap().getOriginalDescription().equals(bitmapName);
        }
    }

    public class DefaultQuery extends DrawEventQuery {
        boolean isTrue;

        public DefaultQuery(boolean isTrue) {
            super(null);
            this.isTrue = isTrue;
        }

        @Override protected boolean isTrueFor(DrawEvent drawEvent) {
            return isTrue;
        }
    }
}
