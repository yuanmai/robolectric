package com.xtremelabs.robolectric.bytecode;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static com.xtremelabs.robolectric.Robolectric.bindShadowClass;
import static com.xtremelabs.robolectric.Robolectric.directlyOn;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(WithTestDefaultsRunner.class)
public class AndroidTranslatorTest {
    @Test
    public void testStaticMethodsAreDelegated() throws Exception {
        Robolectric.bindShadowClass(ShadowAccountManagerForTests.class);

        Context context = mock(Context.class);
        AccountManager.get(context);
        assertThat(ShadowAccountManagerForTests.wasCalled, is(true));
        assertThat(ShadowAccountManagerForTests.context, sameInstance(context));
    }

    @Test
    public void testProtectedMethodsAreDelegated() throws Exception {
        Robolectric.bindShadowClass(ShadowClassWithProtectedMethod.class);

        ClassWithProtectedMethod overlay = new ClassWithProtectedMethod();
        assertEquals("shadow name", overlay.getName());
    }

    @Test
    public void testNativeMethodsAreDelegated() throws Exception {
        Robolectric.bindShadowClass(ShadowPaintForTests.class);

        Paint paint = new Paint();
        paint.setColor(1234);

        assertThat(paint.getColor(), is(1234));
    }

    @Test
    public void testPrintlnWorks() throws Exception {
        Log.println(1, "tag", "msg");
    }

    @Test
    public void whenShadowedClassHasNoDefaultConstructor_generatedDefaultConstructorShouldNotCallShadow() throws Exception {
        Robolectric.bindShadowClass(ShadowClassWithNoDefaultConstructor.class);

        Constructor<ClassWithNoDefaultConstructor> ctor = ClassWithNoDefaultConstructor.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ClassWithNoDefaultConstructor instance = ctor.newInstance();
        assertThat(Robolectric.shadowOf_(instance), not(nullValue()));
        assertThat(Robolectric.shadowOf_(instance), instanceOf(ShadowClassWithNoDefaultConstructor.class));
    }

    @Test
    public void directlyOn_shouldCallThroughToOriginalMethodBody() throws Exception {
        Robolectric.bindShadowClass(ExceptionThrowingShadowView.class);
        View view = new View(null);

        try {
            view.setClickable(true);
        } catch(RuntimeException expected) {
            assertEquals("shadow setClickable was called", expected.getMessage());
        }
        try {
            view.isClickable();
        } catch(RuntimeException expected) {
            assertEquals("shadow isClickable was called", expected.getMessage());
        }
        
        directlyOn(view).setClickable(true);
        assertTrue(directlyOn(view).isClickable());
        directlyOn(view).setClickable(false);
        assertFalse(directlyOn(view).isClickable());
    }

    @Test
    public void testDirectlyOn_Statics() throws Exception {
        Robolectric.bindShadowClass(ExceptionThrowingShadowView.class);

        try {
            View.resolveSize(0, 0);
        } catch(RuntimeException expected) {
            assertEquals("shadow resolveSize was called", expected.getMessage());
        }

        assertEquals(27, View.resolveSize(27, View.MeasureSpec.UNSPECIFIED));
    }

    @Test
    public void testDirectlyOn_InstanceChecking() throws Exception {
        View view1 = new View(null);
        View view2 = new View(null);

        Exception e = null;
        try {
            directlyOn(view1);
            view2.bringToFront();
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertThat(e.getMessage(), startsWith("expected to perform direct call on <android.view.View"));
        assertThat(e.getMessage(), containsString("> but got <android.view.View"));
    }

    @Test
    public void testDirectlyOn_Statics_InstanceChecking() throws Exception {
        Robolectric.bindShadowClass(TextViewWithDummyGetTextColorsMethod.class);
        assertNotNull(TextView.getTextColors(null, null)); // the real implementation would asplode

        Exception e = null;
        try {
            directlyOn(View.class);
            TextView.getTextColors(null, null);
        } catch (RuntimeException e1) {
            e = e1;
        }

        assertNotNull(e);
        assertThat(e.getMessage(), equalTo("expected to perform direct call on <class android.view.View> but got <class android.widget.TextView>"));
    }

    // same test repeated twice in order to check between-test behavior
    @Test(expected=IllegalStateException.class)
    public void shouldClearCallDirectlyStateBeforeEachTest() throws Exception {
        assertNull(AndroidTranslator.ALL_VARS.get().callDirectly);
        directlyOn(View.class);
    }

    @Test(expected=IllegalStateException.class)
    public void shouldClearCallDirectlyStateAfterEachTest() throws Exception {
        assertNull(AndroidTranslator.ALL_VARS.get().callDirectly);
        directlyOn(View.class);
    }

    @Test
    public void testDirectlyOn_CallTwiceChecking() throws Exception {
        directlyOn(View.class);

        Exception e = null;
        try {
            directlyOn(View.class);
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertThat(e.getMessage(), equalTo("already expecting a direct call on <class android.view.View> but here's a new request for <class android.view.View>"));
    }

    @Test
    public void shouldDelegateToObjectToStringIfShadowHasNone() throws Exception {
        assertTrue(new View(null).toString().startsWith("android.view.View@"));
    }

    @Test
    public void shouldDelegateToObjectHashCodeIfShadowHasNone() throws Exception {
        Robolectric.bindShadowClass(ViewWithoutHashCodeMethod.class);
        View view = new View(null);

        assertEquals(view.hashCode(), directlyOn(view).hashCode());
        assertTrue(view.hashCode() != 0);
    }

    @Test
    public void shouldDelegateToObjectEqualsIfShadowHasNone() throws Exception {
        View view = new View(null);
        assertEquals(view, view);
    }

    @Test
    public void shouldGenerateSeparatedConstructorBodies() throws Exception {
//        Robolectric.bindShadowClass(ShadowOfClassWithSomeConstructors.class);
        ClassWithSomeConstructors o = new ClassWithSomeConstructors("my name");
        Method realConstructor = o.getClass().getMethod("__constructor__", String.class);
        realConstructor.invoke(o, "my name");
        assertEquals("my name", o.name);
    }

    @Test
    public void shouldCallOriginalConstructorBodySomehow() throws Exception {
        Robolectric.bindShadowClass(ShadowOfClassWithSomeConstructors.class);
        ClassWithSomeConstructors o = new ClassWithSomeConstructors("my name");
        assertEquals("my name", o.name);
    }

    @Ignore @Test
    public void whenClassIsUnshadowed_shouldPerformStaticInitialization() throws Exception {
        assertEquals("Floyd", ClassWithStaticInitializer.name);
    }
    
    @Test
    public void whenClassIsShadowed_shouldDeferStaticInitialization() throws Exception {
        bindShadowClass(ShadowClassWithStaticInitializer.class);
        assertEquals(null, ClassWithStaticInitializer.name);

        AndroidTranslator.performStaticInitialization(ClassWithStaticInitializer.class);
        assertEquals("Floyd", ClassWithStaticInitializer.name);
    }

    @Implements(ClassWithProtectedMethod.class)
    public static class ShadowClassWithProtectedMethod {
        @Implementation
        protected String getName() {
            return "shadow name";
        }
    }

    @Instrument
    public static class ClassWithProtectedMethod {
        protected String getName() {
            return "protected name";
        }
    }

    @Implements(Paint.class)
    public static class ShadowPaintForTests {
        private int color;

        @Implementation
        public void setColor(int color) {
            this.color = color;
        }

        @Implementation
        public int getColor() {
            return color;
        }
    }

    @Implements(AccountManager.class)
    public static class ShadowAccountManagerForTests {
        public static boolean wasCalled = false;
        public static Context context;

        public static AccountManager get(Context context) {
            wasCalled = true;
            ShadowAccountManagerForTests.context = context;
            return mock(AccountManager.class);
        }
    }

    @Implements(View.class)
    public static class ExceptionThrowingShadowView {
        @Implementation
        public void setClickable(boolean clickable) {
            throw new RuntimeException("shadow setClickable was called");
        }
        @Implementation
        public boolean isClickable() {
            throw new RuntimeException("shadow isClickable was called");
        }
        @Implementation
        public static int resolveSize(int size, int measureSpec) {
            throw new RuntimeException("shadow resolveSize was called");
        }
    }

    @Implements(TextView.class)
    public static class TextViewWithDummyGetTextColorsMethod {
        public static ColorStateList getTextColors(Context context, TypedArray attrs) {
            return new ColorStateList(new int[0][0], new int[0]);
        }
    }

    @Implements(View.class)
    public static class ViewWithoutHashCodeMethod {
    }

    @Implements(ClassWithNoDefaultConstructor.class)
    public static class ShadowClassWithNoDefaultConstructor {
    }

    @Instrument
    @SuppressWarnings({"UnusedDeclaration"})
    public static class ClassWithNoDefaultConstructor {
        ClassWithNoDefaultConstructor(String string) {
        }
    }

    @Instrument
    public static class ClassWithSomeConstructors {
        private String name;

        public ClassWithSomeConstructors(String name) {
            this.name = name;
        }
    }

    @Implements(AndroidTranslatorTest.ClassWithSomeConstructors.class)
    public static class ShadowOfClassWithSomeConstructors {
    }

    @Instrument
    public static class ClassWithStaticInitializer {
        static String name = "Floyd";
    }

    @Implements(AndroidTranslatorTest.ClassWithStaticInitializer.class)
    public static class ShadowClassWithStaticInitializer {
    }
}
