package com.xtremelabs.robolectric.internal;

/**
 * Indicates that a class should not be instrumented by AndroidTranslator under any circumstances.
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
public @interface DoNotInstrument {
}
