package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.TestAttributeSet;

@Implements(Preference.class)
public class ShadowPreference {

	private Context context;
	private AttributeSet attributeSet;

	public void __constructor__(Context context, AttributeSet attributeSet) {
		this.context = context;
		this.attributeSet = attributeSet;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Implementation
	public Context getContext() {
		return context;
	}

	@Implementation
	public boolean isEnabled() {
		return getAttributeBooleanValue("android", "enabled", true);
	}

	@Implementation
	public void setEnabled(boolean enabled) {
		if (attributeSet != null && attributeSet instanceof TestAttributeSet<?>) {
			((TestAttributeSet<?>) attributeSet).setAttributeValue("android", "enabled", String.valueOf(enabled));
		}
	}

	@Implementation
	public String getKey() {
		return getAttributeValue("android", "key");
	}

	@Implementation
	public CharSequence getSummary() {
		return getAttributeValue("android", "summary");
	}

	@Implementation
	public void setSummary(CharSequence summary) {
		if (attributeSet != null && attributeSet instanceof TestAttributeSet<?>) {
			((TestAttributeSet<?>) attributeSet).setAttributeValue("android", "summary", summary);
		}
	}

	@Implementation
	public CharSequence getTitle() {
		return getAttributeValue("android", "title");
	}

	@Implementation
	public void setTitle(CharSequence title) {
		if (attributeSet != null && attributeSet instanceof TestAttributeSet<?>) {
			((TestAttributeSet<?>) attributeSet).setAttributeValue("android", "title", title);
		}
	}

	@Implementation
	public boolean hasKey() {
		String key = getKey();
		return key != null && key.trim().length() > 0;
	}

	private String getAttributeValue(String namespace, String name) {
		if (attributeSet != null) {
			String text = attributeSet.getAttributeValue(namespace, name);
			if (text != null) {
				if (text.startsWith("@string/")) {
					int textResId = attributeSet.getAttributeResourceValue("android", name, 0);
					text = context.getResources().getString(textResId);
				}
			}
			return text;
		}
		return null;
	}

	private boolean getAttributeBooleanValue(String namespace, String name) {
		if (attributeSet != null) {
			return attributeSet.getAttributeBooleanValue(namespace, name, false);
		}
		return false;
	}

	private boolean getAttributeBooleanValue(String namespace, String name, boolean defaultValue) {
		if (attributeSet != null) {
			return attributeSet.getAttributeBooleanValue(namespace, name, defaultValue);
		}
		return false;
	}
}
