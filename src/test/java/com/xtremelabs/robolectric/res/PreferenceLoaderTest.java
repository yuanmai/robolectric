package com.xtremelabs.robolectric.res;

import android.app.Activity;
import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceLoaderTest {
    private PreferenceLoader preferenceLoader;
    private Context context;

    @Before
    public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();

        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(resourceFile("res", "values"));
        preferenceLoader = new PreferenceLoader(resourceExtractor, new AttrResourceLoader(resourceExtractor));
        new DocumentLoader(preferenceLoader).loadResourceXmlDir(resourceFile("res", "xml"));

        context = new Activity();
        // ShadowWrangler.getInstance().debug = true;
    }

    @Test
    public void testPreferences() throws Exception {
        PreferenceGroup preferenceScreen = (PreferenceGroup) preferenceLoader.inflatePreference(context, R.xml.preferences);
        TestUtil.assertInstanceOf(PreferenceScreen.class, preferenceScreen);

        assertSame(context, preferenceScreen.getContext());
        assertEquals(2, preferenceScreen.getPreferenceCount());
        assertNull(preferenceScreen.getTitle());
        assertFalse(preferenceScreen.hasKey());
        PreferenceGroup preferenceCategory = (PreferenceGroup) preferenceScreen.getPreference(0);
        assertNotNull(preferenceCategory);
        assertEquals("preference_category", preferenceCategory.getTitle());
        TestUtil.assertInstanceOf(PreferenceCategory.class, preferenceCategory);
        assertFalse(preferenceScreen.hasKey());
        assertEquals(2, preferenceCategory.getPreferenceCount());

        ListPreference listPreference = (ListPreference) preferenceCategory.getPreference(0);
        assertTrue(listPreference.hasKey());

        EditTextPreference editTextPreference = (EditTextPreference) preferenceScreen.findPreference("PREFERENCE_TEST_EDITTEXTPREFERENCE");
        assertNotNull(editTextPreference);
        assertEquals("edittextpreference_title", editTextPreference.getTitle());
        assertEquals("edittextpreference_summary", editTextPreference.getSummary());
        assertTrue(editTextPreference.isEnabled());
        editTextPreference.setEnabled(false);
        assertFalse(editTextPreference.isEnabled());

        editTextPreference.setTitle("A test title");
        assertEquals("A test title", editTextPreference.getTitle());

        editTextPreference.setSummary("A test summary");
        assertEquals("A test summary", editTextPreference.getSummary());

        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preferenceScreen.findPreference("PREFERENCE_HOMER_SIMPSON");
        assertNotNull(checkBoxPreference);
        assertFalse(checkBoxPreference.isEnabled());
        assertEquals("Donuts", checkBoxPreference.getTitle());
    }
}
