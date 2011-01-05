package com.xtremelabs.robolectric.internal;

/**
 * Indicates that a class definition shadows a class that is not always expected to be present at run time and prevents
 * warnings from being emitted when the class is not found.
 *
 * @see com.xtremelabs.robolectric.internal.Implements
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
public @interface Optional {
}
