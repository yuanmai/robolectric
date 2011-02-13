package com.xtremelabs.robolectric.shadows;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.preference.PreferenceActivity;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestPreferenceActivity;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceActivityTest {

	private PreferenceActivity preferenceActivity;
	private ShadowPreferenceActivity shadowPreferenceActivity;

	@Before
	public void setUp() throws Exception {
		preferenceActivity = Robolectric.newInstanceOf(TestPreferenceActivity.class);
		shadowPreferenceActivity = Robolectric.shadowOf(preferenceActivity);
	}

	@Test
	public void testPreferenceScreen() {
		shadowPreferenceActivity.addPreferencesFromResource(R.xml.preferences);
		assertNotNull(preferenceActivity.getPreferenceScreen());
	}

	@Test
	public void testFindPreference() {
		shadowPreferenceActivity.addPreferencesFromResource(R.xml.preferences);
		assertNotNull(preferenceActivity.findPreference("PREFERENCE_HOMER_SIMPSON"));
	}
}
