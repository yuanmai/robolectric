package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.internal.RobolectricTestRunnerInterface;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

public class RobolectricClassLoader extends javassist.Loader {
    private ClassCache classCache;

    public RobolectricClassLoader(ClassHandler classHandler) {
        this(RobolectricClassLoader.class.getClassLoader(), classHandler);
    }

    public RobolectricClassLoader(ClassLoader classLoader, ClassHandler classHandler) {
        super(classLoader, null);

        delegateLoadingOf(AndroidTranslator.class.getName());
        delegateLoadingOf(ClassHandler.class.getName());

        classCache = new ClassCache(
                RobolectricTestRunner.USE_REAL_ANDROID_SOURCES
                        ? "tmp/cached-robolectrified-REAL-ANDROID-classes.jar"
                        : "tmp/cached-robolectric-classes.jar", AndroidTranslator.CACHE_VERSION);
        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(classLoader));

            if (classLoader != RobolectricClassLoader.class.getClassLoader()) {
                classPool.appendClassPath(new LoaderClassPath(RobolectricClassLoader.class.getClassLoader()));
            }

            AndroidTranslator androidTranslator = new AndroidTranslator(classHandler, classCache);
            addTranslator(classPool, androidTranslator);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        if (RobolectricTestRunner.USE_REAL_ANDROID_SOURCES) {
            boolean shouldComeFromThisClassLoader = !(
                    name.startsWith("org.junit")
                            || name.startsWith("org.hamcrest")
                            || name.startsWith("javassist")
                            || name.equals(AndroidTranslator.class.getName())
                            || name.equals(ClassHandler.class.getName())
                            || name.equals(RealObject.class.getName())
                            || name.equals(ShadowWrangler.class.getName())
                            || name.equals(RobolectricTestRunnerInterface.class.getName())
                            || name.equals(Vars.class.getName())
            );

//            System.err.println(name + " should come from class loader? " + shouldComeFromThisClassLoader);

            Class<?> theClass;
            if (shouldComeFromThisClassLoader) {
                theClass = super.loadClass(name);
            } else {
                theClass = RobolectricClassLoader.class.getClassLoader().loadClass(name);
            }

//            try {
//                if (name.contains("Activity"))
//                    System.out.println("method: " + theClass.getDeclaredMethod("onPause"));
//            } catch (NoSuchMethodException e) {
//            }

            return theClass;
        } else {
            return loadClassOld(name);
        }
    }

    private Class loadClassOld(String name) throws ClassNotFoundException {
        boolean shouldComeFromThisClassLoader = !(name.startsWith("org.junit") || name.startsWith("org.hamcrest"));

        Class<?> theClass;
        if (shouldComeFromThisClassLoader) {
            theClass = super.loadClass(name);
        } else {
            theClass = getParent().loadClass(name);
        }

        return theClass;
    }

    public Class<?> bootstrap(Class testClass) {
        String testClassName = testClass.getName();

        try {
            return loadClass(testClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't load " + testClassName, e);
        }
    }

    @Override protected Class findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = classCache.getClassBytesFor(name);
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }
}
