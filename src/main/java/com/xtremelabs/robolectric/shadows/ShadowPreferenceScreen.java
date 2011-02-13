package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings( { "UnusedDeclaration" })
@Implements(PreferenceScreen.class)
public class ShadowPreferenceScreen extends ShadowPreferenceGroup {

	@Implementation
	@Override
	public boolean addPreference(Preference preference) {
		return super.addPreference(preference);
	}

	@Implementation
	@Override
	public Context getContext() {
		return super.getContext();
	}

	@Override
	public void setContext(Context context) {
		super.setContext(context);
	}

}
