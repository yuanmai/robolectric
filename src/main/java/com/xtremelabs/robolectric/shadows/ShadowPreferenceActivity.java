package com.xtremelabs.robolectric.shadows;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(PreferenceActivity.class)
public class ShadowPreferenceActivity extends ShadowListActivity {

    private Preference preference;

    @Implementation
    public void addPreferencesFromResource(int preferencesResId) {
        preference = shadowOf(getApplicationContext()).getResourceLoader().inflatePreference(getApplicationContext(), preferencesResId);
    }

    @Implementation
    public Preference findPreference(CharSequence key) {
        if (preference != null && preference instanceof PreferenceGroup) {
            return ((PreferenceGroup) preference).findPreference(key);
        }
        return null;
    }

    @Implementation
    public PreferenceScreen getPreferenceScreen() {
        if (preference != null && preference instanceof PreferenceScreen) {
            return ((PreferenceScreen) preference);
        }
        return null;
    }

}
