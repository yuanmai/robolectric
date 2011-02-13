package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(PreferenceCategory.class)
public class ShadowPreferenceCategory extends ShadowPreferenceGroup {

    public void __constructor__(Context context, AttributeSet attributeSet) {
        super.__constructor__(context, attributeSet);
    }

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
