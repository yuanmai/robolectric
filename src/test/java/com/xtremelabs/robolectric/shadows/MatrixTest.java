package com.xtremelabs.robolectric.shadows;

import android.graphics.Matrix;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class MatrixTest {
    @Test
    public void shouldHaveDescription() throws Exception {
        Matrix matrix = Robolectric.newInstanceOf(Matrix.class);

        matrix.setTranslate(1.2f, 3.4f);
        matrix.setScale(5.6f, 7.8f);

        assertEquals("translateX: 1.2, translateY: 3.4, scaleX: 5.6, scaleY: 7.8", shadowOf(matrix).getDescription());
    }

    @Test
    public void descriptionShouldIgnoreZeroTranslation() throws Exception {
        Matrix matrix = Robolectric.newInstanceOf(Matrix.class);

        matrix.setScale(5.6f, 7.8f);

        assertEquals("scaleX: 5.6, scaleY: 7.8", shadowOf(matrix).getDescription());
    }

    @Test
    public void descriptionShouldIgnoreScaleOfOne() throws Exception {
        Matrix matrix = Robolectric.newInstanceOf(Matrix.class);

        matrix.setTranslate(1.2f, 3.4f);

        assertEquals("translateX: 1.2, translateY: 3.4", shadowOf(matrix).getDescription());
    }
}
