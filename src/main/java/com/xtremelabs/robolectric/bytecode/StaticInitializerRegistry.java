package com.xtremelabs.robolectric.bytecode;

import java.util.HashSet;
import java.util.Set;

public class StaticInitializerRegistry {
    boolean stillDeferring = true;
    Set<Class<?>> deferredInitializeClasses = new HashSet<Class<?>>();
    Set<Class<?>> neverInitializeClasses = new HashSet<Class<?>>();

    public void deferInitializationOf(Class<?> clazz) {
        if (!neverInitializeClasses.contains(clazz)) {
            if (stillDeferring) {
                deferredInitializeClasses.add(clazz);
            } else {
                AndroidTranslator.performStaticInitialization(clazz);
            }
        }
    }

    public void neverInitialize(Class<?> clazz) {
        deferredInitializeClasses.remove(clazz);
        neverInitializeClasses.add(clazz);
    }

    public void runDeferredInitializers() {
        for (Class<?> clazz : deferredInitializeClasses) {
            AndroidTranslator.performStaticInitialization(clazz);
        }
        deferredInitializeClasses.clear();
        stillDeferring = false;
    }
}
