package com.xtremelabs.robolectric.bytecode;

import com.google.android.maps.MapActivity;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithoutTestDefaultsRunner;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;
import com.xtremelabs.robolectric.internal.RealObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

@RunWith(WithoutTestDefaultsRunner.class)
public class ShadowWranglerTest {
    private String name;
    private ShadowWrangler testShadowWrangler;

    @Before
    public void setUp() throws Exception {
        name = "context";
        // in case they haven't been run yet by another TestRunner
        ShadowWrangler.getInstance().runDeferredStaticInitializers();

        testShadowWrangler = ShadowWrangler.newShadowWranglerForTest();
    }

    @Test
    public void whenClassIsUnshadowed_shouldPerformStaticInitialization() throws Exception {
        assertEquals("Hank", UnshadowedClassWithStaticInitializer.name);
    }

    @Test
    public void whenClassIsShadowed_shouldBlockStaticInitialization() throws Exception {
        Robolectric.bindShadowClass(ShadowClassWithStaticInitializer.class);

        assertEquals(null, ClassWithStaticInitializer.name);

        AndroidTranslator.performStaticInitialization(ClassWithStaticInitializer.class);
        assertEquals("Floyd", ClassWithStaticInitializer.name);
    }

    @Test
    public void testConstructorInvocation_WithDefaultConstructorAndNoConstructorDelegateOnShadowClass() throws Exception {
        Robolectric.bindShadowClass(ShadowFoo_WithDefaultConstructorAndNoConstructorDelegate.class);

        Foo foo = new Foo(name);
        assertEquals(ShadowFoo_WithDefaultConstructorAndNoConstructorDelegate.class, Robolectric.shadowOf_(foo).getClass());
    }

    @Test
    public void testConstructorInvocation() throws Exception {
        Robolectric.bindShadowClass(ShadowFoo.class);

        Foo foo = new Foo(name);
        assertSame(name, shadowOf(foo).name);
        assertSame(foo, shadowOf(foo).realFooCtor);
    }

    @Test
    public void testRealObjectAnnotatedFieldsAreSetBeforeConstructorIsCalled() throws Exception {
        Robolectric.bindShadowClass(ShadowFoo.class);

        Foo foo = new Foo(name);
        assertSame(name, shadowOf(foo).name);
        assertSame(foo, shadowOf(foo).realFooField);

        assertSame(foo, shadowOf(foo).realFooInConstructor);
        assertSame(foo, shadowOf(foo).realFooInParentConstructor);
    }

    @Test
    public void testMethodDelegation() throws Exception {
        Robolectric.bindShadowClass(ShadowFoo.class);

        Foo foo = new Foo(name);
        assertSame(name, foo.getName());
    }

    @Test
    public void testEqualsMethodDelegation() throws Exception {
        Robolectric.bindShadowClass(WithEquals.class);

        Foo foo1 = new Foo(name);
        Foo foo2 = new Foo(name);
        assertEquals(foo1, foo2);
    }

    @Test
    public void testHashCodeMethodDelegation() throws Exception {
        Robolectric.bindShadowClass(WithEquals.class);

        Foo foo = new Foo(name);
        assertEquals(42, foo.hashCode());
    }

    @Test
    public void testToStringMethodDelegation() throws Exception {
        Robolectric.bindShadowClass(WithToString.class);

        Foo foo = new Foo(name);
        assertEquals("the expected string", foo.toString());
    }

    @Test
    public void testShadowSelectionSearchesSuperclasses() throws Exception {
        Robolectric.bindShadowClass(ShadowFoo.class);

        TextFoo textFoo = new TextFoo(name);
        assertEquals(ShadowFoo.class, Robolectric.shadowOf_(textFoo).getClass());
    }

    @Test
    public void shouldUseMostSpecificShadow() throws Exception {
        Robolectric.bindShadowClass(ShadowFoo.class);
        Robolectric.bindShadowClass(ShadowTextFoo.class);

        TextFoo textFoo = new TextFoo(name);
        assertThat(shadowOf(textFoo), instanceOf(ShadowTextFoo.class));
    }

    @Test
    public void testPrimitiveArrays() throws Exception {
        Class<?> objArrayClass = ShadowWrangler.loadClass("java.lang.Object[]", getClass().getClassLoader());
        assertTrue(objArrayClass.isArray());
        assertEquals(Object.class, objArrayClass.getComponentType());

        Class<?> intArrayClass = ShadowWrangler.loadClass("int[]", getClass().getClassLoader());
        assertTrue(intArrayClass.isArray());
        assertEquals(Integer.TYPE, intArrayClass.getComponentType());
    }

    @Test
    public void shouldRemoveNoiseFromStackTraces() throws Exception {
        Robolectric.bindShadowClass(ExceptionThrowingShadowFoo.class);
        Foo foo = new Foo(null);

        Exception e = null;
        try {
            foo.getName();
        } catch (Exception e1) {
            e = e1;
        }

        assertNotNull(e);
        assertEquals(IOException.class, e.getClass());
        assertEquals("fake exception", e.getMessage());
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.getBuffer().toString();

        assertThat(stackTrace, containsString("fake exception"));
        assertThat(stackTrace, containsString(ExceptionThrowingShadowFoo.class.getName() + ".getName("));
        assertThat(stackTrace, containsString(Foo.class.getName() + ".getName("));
        assertThat(stackTrace, containsString(ShadowWranglerTest.class.getName() + ".shouldRemoveNoiseFromStackTraces"));

        assertThat(stackTrace, not(containsString("sun.reflect")));
        assertThat(stackTrace, not(containsString("java.lang.reflect")));
        assertThat(stackTrace, not(containsString(ShadowWrangler.class.getName() + ".")));
        assertThat(stackTrace, not(containsString(RobolectricInternals.class.getName() + ".")));
    }

    @Test
    public void shouldIdentifyShadowClasses() throws Exception {

        Class<ShadowClassWithStaticInitializer> shadowClass = ShadowClassWithStaticInitializer.class;
        Class<ClassWithStaticInitializer> unshadowedClass = ClassWithStaticInitializer.class;

        assertTrue(ShadowWrangler.isShadowClass(shadowClass));
        assertFalse(ShadowWrangler.isShadowClass(unshadowedClass));
    }

    @Test
    public void shouldKnowMapsClassesAreStubbed() throws Exception {
        assertTrue(testShadowWrangler.classIsStubbed(MapActivity.class));
    }

    @Test
    public void stubbedPackageList_ShouldContainMapsPackage() throws Exception {
        List<String> stubbedPackageList = testShadowWrangler.getStubbedPackages();
        assertTrue(stubbedPackageList.contains("com.google.android.maps"));
    }

    @Test
    public void shouldAllowStubbedPackageListToBeChanged() throws Exception {
        List<String> stubbedPackageList = testShadowWrangler.getStubbedPackages();
        stubbedPackageList.remove("com.google.android.maps");
        assertFalse(testShadowWrangler.classIsStubbed(MapActivity.class));
    }
    public static boolean isShadowClass(Class<?> shadowClass) {
        return shadowClass.getAnnotation(Implements.class) != null;
    }

    private ShadowFoo shadowOf(Foo foo) {
        return (ShadowFoo) Robolectric.shadowOf_(foo);
    }

    private ShadowTextFoo shadowOf(TextFoo foo) {
        return (ShadowTextFoo) Robolectric.shadowOf_(foo);
    }

    @Implements(Foo.class)
    public static class WithEquals {
        @Override
        public boolean equals(Object o) {
            return true;
        }


        @Override
        public int hashCode() {
            return 42;
        }

    }

    @Implements(Foo.class)
    public static class WithToString {
        @Override
        public String toString() {
            return "the expected string";
        }
    }

    @Implements(TextFoo.class)
    public static class ShadowTextFoo {
    }

    @Instrument
    public static class TextFoo extends Foo {
        public TextFoo(String s) {
            super(s);
        }
    }

    @Implements(Foo.class)
    public static class ShadowFooParent {
        @RealObject
        private Foo realFoo;
        Foo realFooInParentConstructor;

        public void __constructor__(String name) {
            realFooInParentConstructor = realFoo;
        }
    }

    @Implements(Foo.class)
    public static class ShadowFoo_WithDefaultConstructorAndNoConstructorDelegate {
    }

    @Implements(Foo.class)
    public static class ExceptionThrowingShadowFoo {
        @SuppressWarnings({"UnusedDeclaration"})
        public String getName() throws IOException {
            throw new IOException("fake exception");
        }
    }

    @Instrument
    public static class UnshadowedClassWithStaticInitializer {
        static String name = "Hank";
    }

    @Instrument
    public static class ClassWithStaticInitializer {
        static String name = "Floyd";
    }

    @Implements(ClassWithStaticInitializer.class)
    public static class ShadowClassWithStaticInitializer {
        public static boolean hasBeenStaticallyInitialized = false;

        public static void __staticInitializer__() {
            hasBeenStaticallyInitialized = true;
        }
    }
}
