package com.xtremelabs.robolectric;

import android.app.Application;
import android.view.View;
import com.xtremelabs.robolectric.bytecode.AndroidTranslator;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import static com.xtremelabs.robolectric.Robolectric.directlyOn;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunnerTest.RunnerForTesting.class)
public class RobolectricTestRunnerTest {

    @After
    public void tearDown() {
        assertNull(AndroidTranslator.ALL_VARS.get().callDirectly);
    }

    @Test(expected=IllegalStateException.class)
    public void shouldNotAllowDanglingCallsToDirectlyOn() throws Exception {
        directlyOn(View.class);
    }

    @Test
    public void shouldInitializeAndBindApplicationButNotCallOnCreate() throws Exception {
        assertNotNull(Robolectric.application);
        assertEquals(MyTestApplication.class, Robolectric.application.getClass());
        assertFalse(((MyTestApplication) Robolectric.application).onCreateWasCalled);
        assertNotNull(shadowOf(Robolectric.application).getResourceLoader());
    }

    public static class RunnerForTesting extends WithTestDefaultsRunner {
        public RunnerForTesting(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        @Override protected Application createApplication() {
            return new MyTestApplication();
        }
    }

    private static class MyTestApplication extends Application {
        private boolean onCreateWasCalled;

        @Override public void onCreate() {
            this.onCreateWasCalled = true;
        }
    }
}
