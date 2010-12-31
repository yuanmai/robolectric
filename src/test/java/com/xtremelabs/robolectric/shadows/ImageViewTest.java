package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.widget.ImageView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.visualize;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ImageViewTest {
    private ImageView imageView;

    @Before
    public void setUp() throws Exception {
        Resources resources = Robolectric.application.getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image);
        imageView = new ImageView(Robolectric.application);
        imageView.setImageBitmap(bitmap);
    }

    @Test
    public void shouldDrawWithImageMatrix() throws Exception {
        imageView.setImageMatrix(new Matrix());
        assertThat(visualize(imageView), containsString("Bitmap for resource: drawable/an_image"));

        Matrix matrix = new Matrix();
        matrix.setTranslate(15, 20);
        imageView.setImageMatrix(matrix);
        assertThat(visualize(imageView), containsString("drawBitmap: left: 15.0, top: 20.0"));
        assertThat(visualize(imageView), containsString("Bitmap for resource: drawable/an_image"));
    }

    @Test
    public void shouldCopyMatrixSetup() throws Exception {
        Matrix matrix = new Matrix();
        matrix.setTranslate(15, 20);
        imageView.setImageMatrix(matrix);
        assertThat(visualize(imageView), containsString("drawBitmap: left: 15.0, top: 20.0"));
        assertThat(visualize(imageView), containsString("Bitmap for resource: drawable/an_image"));

        matrix.setTranslate(30, 40);
        assertThat(visualize(imageView), containsString("drawBitmap: left: 15.0, top: 20.0"));
        assertThat(visualize(imageView), containsString("Bitmap for resource: drawable/an_image"));

        imageView.setImageMatrix(matrix);
        assertThat(visualize(imageView), containsString("drawBitmap: left: 30.0, top: 40.0"));
        assertThat(visualize(imageView), containsString("Bitmap for resource: drawable/an_image"));
    }
}
