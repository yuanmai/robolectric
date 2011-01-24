package com.xtremelabs.robolectric.bytecode;

import java.util.HashSet;
import java.util.Set;

public class StaticInitializerRegistry {
    private boolean stillDeferring = true;
    private Set<Class<?>> deferredInitializeClasses = new HashSet<Class<?>>();
    private Set<Class<?>> neverInitializeClasses = new HashSet<Class<?>>();

    public void deferInitializationOf(Class<?> clazz) {
        if (!neverInitializeClasses.contains(clazz)) {
            if (stillDeferring) {
                deferredInitializeClasses.add(clazz);
            } else {
                AndroidTranslator.performStaticInitialization(clazz);
            }
        }
    }

    public void initializeUsingShadow(Class<?> clazz, Class<?> shadowClass) {
        deferredInitializeClasses.remove(clazz);
        neverInitializeClasses.add(clazz);
        deferInitializationOf(shadowClass);
    }

    public void runDeferredInitializers() {
        stillDeferring = false;
        for (Class<?> clazz : deferredInitializeClasses) {
            AndroidTranslator.performStaticInitialization(clazz);
        }
        deferredInitializeClasses.clear();
    }
}
