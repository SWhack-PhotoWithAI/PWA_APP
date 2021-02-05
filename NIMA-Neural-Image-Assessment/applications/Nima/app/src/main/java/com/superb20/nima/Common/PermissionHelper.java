package com.superb20.nima.Common;

/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Helper to ask camera permission.
 */
public final class PermissionHelper {
    private static final int STORAGE_PERMISSION_CODE = 1;
    private static final String STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

    /**
     * Check to see we have the necessary permissions for this app.
     */
    public static boolean hasStoragePermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, STORAGE_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Check to see we have the necessary permissions for this app, and ask for them if we don't.
     */
    public static void requestStoragePermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[]{STORAGE_PERMISSION}, STORAGE_PERMISSION_CODE);
    }


    /**
     * Check to see if we need to show the rationale for this permission.
     */
    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, STORAGE_PERMISSION);
    }

    /**
     * Launch Application Setting to grant permission.
     */
    public static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
