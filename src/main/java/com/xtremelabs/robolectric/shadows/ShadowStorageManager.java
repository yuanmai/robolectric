package com.xtremelabs.robolectric.shadows;

import android.os.storage.StorageManager;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * A shadow for StorageManager
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(StorageManager.class)
public class ShadowStorageManager {
    public void __constructor__() {
    }
}
