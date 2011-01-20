package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithoutTestDefaultsRunner;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;
import com.xtremelabs.robolectric.internal.RealObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.directlyOn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(WithoutTestDefaultsRunner.class)
public class SubclassMethodLookupTest {
    @Test
    public void shouldNotCallShadowsForCallsToSuper() throws Exception {
        Robolectric.bindShadowClass(ShadowSuperClass.class);
        Robolectric.bindShadowClass(ShadowSubClass.class);

        assertEquals("sub shadowed[from sub(blah) from super(blah)]",
                new SubClass().method("blah"));
    }

    @Test
    public void shouldNotInfinitelyRecurseOnToString() throws Exception {
        Robolectric.bindShadowClass(ShadowSuperClass.class);
        Robolectric.bindShadowClass(ShadowSubClass.class);

        assertTrue(new SubClass().toString() != null);
    }
    
    @Test
    public void shouldCorrectlyImplementToStringEtc() throws Exception {
        assertEquals("here i am!", new SubHasToString().toString());
    }

    @Test
    public void shouldCorrectlyImplementToStringEtcForSubSubclasses() throws Exception {
        Robolectric.bindShadowClass(ShadowSubSubHasToString.class);
        assertEquals("shadowed here i am!", new SubSubHasToString().toString());
    }

    @Instrument
    static public class SuperClass {
        String method(String arg) {
            return "from super(" + arg + ")";
        }
    }

    @Implements(SuperClass.class)
    static public class ShadowSuperClass {
        @RealObject SuperClass superClass;

        @Implementation
        String method(String arg) {
            return "super shadowed[" + directlyOn(superClass).method(arg) + "]";
        }
    }

    @Instrument
    static public class SubClass extends SuperClass {
        @Override
        String method(String arg) {
            return "from sub(" + arg + ") " + super.method(arg);
        }
    }

    @Implements(SubClass.class)
    static public class ShadowSubClass extends ShadowSuperClass {
        @RealObject SubClass subClass;

        @Implementation
        String method(String arg) {
            return "sub shadowed[" + directlyOn(subClass).method(arg) + "]";
        }
    }

    @Instrument
    static public class SubHasToString extends HasToString {
    }

    @Instrument
    static public class SubSubHasToString extends SubHasToString {
    }

    @Instrument
    static public class HasToString {
        @Override
        public String toString() {
            return "here i am!";
        }
    }

    @Implements(SubclassMethodLookupTest.SubSubHasToString.class)
    static public class ShadowSubSubHasToString {
        @RealObject SubSubHasToString subSubHasToString;
        @Override
        public String toString() {
            return "shadowed " + directlyOn(subSubHasToString).toString();
        }
    }
}
