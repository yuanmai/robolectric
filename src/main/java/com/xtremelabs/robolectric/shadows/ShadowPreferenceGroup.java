package com.xtremelabs.robolectric.shadows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(PreferenceGroup.class)
public class ShadowPreferenceGroup extends ShadowPreference {

	private ArrayList<Preference> childPreferences;
	private HashMap<String, Preference> childPreferencesByKey;

	public void __constructor__(Context context, AttributeSet attributeSet) {
		super.__constructor__(context, attributeSet);
	}

	public ShadowPreferenceGroup() {
		childPreferences = new ArrayList<Preference>();
		childPreferencesByKey = new HashMap<String, Preference>();
	}

	@Implementation
	public boolean addPreference(Preference preference) {
		childPreferences.add(preference);
		if (preference.getKey() != null) {
			childPreferencesByKey.put(preference.getKey(), preference);
		}
		return true;
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

	@Implementation
	public int getPreferenceCount() {
		return childPreferences.size();
	}

	@Implementation
	public Preference getPreference(int index) {
		return childPreferences.get(index);
	}

	@Implementation
	public Preference findPreference(CharSequence key) {
		if (childPreferencesByKey.containsKey(key)) {
			return childPreferencesByKey.get(key);
		} else {
			for (Iterator<Preference> it = childPreferences.iterator(); it.hasNext();) {
				Preference childPreference = it.next();
				if (childPreference instanceof PreferenceGroup) {
					Preference possibleMatchPreference = ((PreferenceGroup) childPreference).findPreference(key);
					if (possibleMatchPreference != null) {
						return possibleMatchPreference;
					}
				}
			}
		}
		return null;
	}
}
