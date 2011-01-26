package com.xtremelabs.robolectric.shadows;

import android.app.DownloadManager;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * A shadow for DownloadManager
 *
 * TODO: Get this to bootstrap the real DownloadManager code
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(DownloadManager.class)
public class ShadowDownloadManager {
    public void __constructor__() {
    }
}