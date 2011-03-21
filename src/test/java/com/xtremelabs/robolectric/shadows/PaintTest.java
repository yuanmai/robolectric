package com.xtremelabs.robolectric.shadows;

import android.graphics.Paint;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(WithTestDefaultsRunner.class)
public class PaintTest {

	@Test
	public void shouldGetIsDitherInfo() {
		Paint paint = Robolectric.newInstanceOf(Paint.class);
		assertFalse(paint.isAntiAlias());
		ShadowPaint shadowPaint = shadowOf(paint);
		shadowPaint.setAntiAlias(true);
		assertTrue(paint.isAntiAlias());		
	}
	
	@Test
	public void shouldGetIsAntiAlias() {
		Paint paint = Robolectric.newInstanceOf(Paint.class);
		assertFalse(paint.isDither());
		ShadowPaint shadowPaint = shadowOf(paint);
		shadowPaint.setDither(true);
		assertTrue(paint.isDither());				
	}
}
