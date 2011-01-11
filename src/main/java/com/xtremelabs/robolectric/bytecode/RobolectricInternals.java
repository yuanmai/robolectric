package com.xtremelabs.robolectric.bytecode;

// keep this class package-local; it is made public by AndroidTranslator at runtime
@SuppressWarnings({"UnusedDeclaration"})
public class RobolectricInternals {
    // initialized via magic by AndroidTranslator
    private static ClassHandler classHandler;

    public static <T> T directlyOn(T shadowedObject) {
        Vars vars = AndroidTranslator.ALL_VARS.get();

        if (vars.callDirectly != null) {
            Object expectedInstance = vars.callDirectly;
            vars.callDirectly = null;
            throw new RuntimeException("already expecting a direct call on <" + expectedInstance + "> but here's a new request for <" + shadowedObject + ">");
        }

        vars.callDirectly = shadowedObject;
        return shadowedObject;
    }

    public static boolean shouldCallDirectly(Object directInstance) {
        Vars vars = AndroidTranslator.ALL_VARS.get();
        Object expectedInstance = vars.callDirectly;
        vars.callDirectly = null;

        if (directInstance == expectedInstance) {
            return true;
        }
        if (expectedInstance == null) {
            return false;
        }
        throw new RuntimeException("expected to perform direct call on <" + expectedInstance + "> but got <" + directInstance + ">");
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static void classInitializing(Class clazz) throws Exception {
        classHandler.classInitializing(clazz);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) throws Exception {
        return classHandler.methodInvoked(clazz, methodName, instance, paramTypes, params);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(Object o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(boolean o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(byte o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(char o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(short o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(int o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(long o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(float o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(double o) {
        return o;
    }
}
