package com.xtremelabs.robolectric.bytecode;

import android.net.Uri;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.DoNotInstrument;
import com.xtremelabs.robolectric.internal.Instrument;
import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.Translator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
public class AndroidTranslator implements Translator {
    /**
     * IMPORTANT -- increment this number when the bytecode generated for modified classes changes
     * so the cache file can be invalidated.
     */
    public static final int CACHE_VERSION = -1;

    private static final List<ClassHandler> CLASS_HANDLERS = new ArrayList<ClassHandler>();
    public static final ThreadLocal<Vars> ALL_VARS = new ThreadLocal<Vars>() {
        @Override
        protected Vars initialValue() {
            return new Vars();
        }
    };
    static final String STATIC_INITIALIZER_METHOD_NAME = "__staticInitializer__";

    private ClassHandler classHandler;
    private ClassCache classCache;

    public AndroidTranslator(ClassHandler classHandler, ClassCache classCache) {
        this.classHandler = classHandler;
        this.classCache = classCache;
    }

    public static ClassHandler getClassHandler(int index) {
        return CLASS_HANDLERS.get(index);
    }

    public static void performStaticInitialization(Class<?> clazz) {
        try {
            if (!ShadowWrangler.isShadowClass(clazz)) {
                Robolectric.directlyOn(clazz);
            }

            Method staticInitializer = clazz.getDeclaredMethod(STATIC_INITIALIZER_METHOD_NAME);
            if (staticInitializer != null) {
                staticInitializer.setAccessible(true);
                staticInitializer.invoke(null);
            }
        } catch (NoSuchMethodException e) {
            if (!ShadowWrangler.isShadowClass(clazz)) {
                throw new RuntimeException(e);
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Can't call " + STATIC_INITIALIZER_METHOD_NAME + " on " + clazz.getSimpleName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            throw new RuntimeException("Can't call " + STATIC_INITIALIZER_METHOD_NAME + " on " + clazz.getSimpleName() + " is it declared 'static'?", e);
        }
    }

    @Override
    public void start(ClassPool classPool) throws NotFoundException, CannotCompileException {
        injectClassHandlerToInstrumentedClasses(classPool);
    }

    private void injectClassHandlerToInstrumentedClasses(ClassPool classPool) throws NotFoundException, CannotCompileException {
        int index;
        synchronized (CLASS_HANDLERS) {
            CLASS_HANDLERS.add(classHandler);
            index = CLASS_HANDLERS.size() - 1;
        }

        CtClass robolectricInternalsCtClass = classPool.get(RobolectricInternals.class.getName());
        robolectricInternalsCtClass.setModifiers(Modifier.PUBLIC);

        robolectricInternalsCtClass.makeClassInitializer().insertBefore("{\n" +
                "classHandler = " + AndroidTranslator.class.getName() + ".getClassHandler(" + index + ");\n" +
                "}");
    }

    @Override
    public void onLoad(ClassPool classPool, String className) throws NotFoundException, CannotCompileException {
        if (classCache.isWriting()) {
            throw new IllegalStateException("shouldn't be modifying bytecode after we've started writing cache! class=" + className);
        }

        if (classHasFromAndroidEquivalent(className)) {
            replaceClassWithFromAndroidEquivalent(classPool, className);
            return;
        }

        CtClass ctClass;
        try {
            ctClass = classPool.get(className);
        } catch (NotFoundException e) {
            throw new IgnorableClassNotFoundException(e);
        }

        boolean wantsToBeInstrumented =
                className.startsWith("android.")
                        || className.startsWith("com.android.layoutlib.")
                        || className.startsWith("com.google.android.maps")
                        || className.equals("org.apache.http.impl.client.DefaultRequestDirector")
                        || ctClass.hasAnnotation(Instrument.class);

        if (wantsToBeInstrumented && !ctClass.hasAnnotation(DoNotInstrument.class)) {
            int modifiers = ctClass.getModifiers();
            if (Modifier.isFinal(modifiers)) {
                ctClass.setModifiers(modifiers & ~Modifier.FINAL);
            }

            if (ctClass.isInterface() || ctClass.isEnum()) return;

            classHandler.instrument(ctClass);

            CtClass superclass = ctClass.getSuperclass();
            if (!superclass.isFrozen()) {
                onLoad(classPool, superclass.getName());
            }

            MethodGenerator methodGenerator = new MethodGenerator(ctClass);
            methodGenerator.deferClassInitialization();
            methodGenerator.fixConstructors();
            methodGenerator.fixMethods();

            try {
                classCache.addClass(className, ctClass.toBytecode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean classHasFromAndroidEquivalent(String className) {
        return className.startsWith(Uri.class.getName());
    }

    private void replaceClassWithFromAndroidEquivalent(ClassPool classPool, String className) throws NotFoundException {
        FromAndroidClassNameParts classNameParts = new FromAndroidClassNameParts(className);
        if (classNameParts.isFromAndroid()) return;

        String from = classNameParts.getNameWithFromAndroid();
        CtClass ctClass = classPool.getAndRename(from, className);

        ClassMap map = new ClassMap() {
            @Override
            public Object get(Object jvmClassName) {
                FromAndroidClassNameParts classNameParts = new FromAndroidClassNameParts(jvmClassName.toString());
                if (classNameParts.isFromAndroid()) {
                    return classNameParts.getNameWithoutFromAndroid();
                } else {
                    return jvmClassName;
                }
            }
        };
        ctClass.replaceClassName(map);
    }

    class FromAndroidClassNameParts {
        private static final String TOKEN = "__FromAndroid";

        private String prefix;
        private String suffix;

        FromAndroidClassNameParts(String name) {
            int dollarIndex = name.indexOf("$");
            prefix = name;
            suffix = "";
            if (dollarIndex > -1) {
                prefix = name.substring(0, dollarIndex);
                suffix = name.substring(dollarIndex);
            }
        }

        public boolean isFromAndroid() {
            return prefix.endsWith(TOKEN);
        }

        public String getNameWithFromAndroid() {
            return prefix + TOKEN + suffix;
        }

        public String getNameWithoutFromAndroid() {
            return prefix.replace(TOKEN, "") + suffix;
        }
    }
}
