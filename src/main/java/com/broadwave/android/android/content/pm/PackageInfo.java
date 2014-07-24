/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.broadwave.android.android.content.pm;

import com.broadwave.android.android.os.Parcelable;

/**
 * Overall information about the contents of a package. This corresponds to all
 * of the information collected from AndroidManifest.xml.
 */
public class PackageInfo implements Parcelable {
    /**
     * The name of this package. From the &lt;manifest&gt; tag's "name"
     * attribute.
     */
    public String packageName;

    /**
     * The version number of this package, as specified by the &lt;manifest&gt;
     * tag's {@link android.R.styleable#AndroidManifest_versionCode versionCode}
     * attribute.
     */
    public int versionCode;

    /**
     * The version name of this package, as specified by the &lt;manifest&gt;
     * tag's {@link android.R.styleable#AndroidManifest_versionName versionName}
     * attribute.
     */
    public String versionName;

    /**
     * The shared user ID name of this package, as specified by the
     * &lt;manifest&gt; tag's
     * {@link android.R.styleable#AndroidManifest_sharedUserId sharedUserId}
     * attribute.
     */
    public String sharedUserId;

    /**
     * The shared user ID label of this package, as specified by the
     * &lt;manifest&gt; tag's
     * {@link android.R.styleable#AndroidManifest_sharedUserLabel
     * sharedUserLabel} attribute.
     */
    public int sharedUserLabel;

    /**
     * Information collected from the &lt;application&gt; tag, or null if there
     * was none.
     */
    public ApplicationInfo applicationInfo;

    /**
     * The time at which the app was first installed. Units are as per
     * {@link System#currentTimeMillis()}.
     */
    public long firstInstallTime;

    /**
     * The time at which the app was last updated. Units are as per
     * {@link System#currentTimeMillis()}.
     */
    public long lastUpdateTime;

    /**
     * All kernel group-IDs that have been assigned to this package. This is
     * only filled in if the flag {@link PackageManager#GET_GIDS} was set.
     */
    public int[] gids;

    /**
     * Array of all {@link android.R.styleable#AndroidManifestActivity
     * &lt;activity&gt;} tags included under &lt;application&gt;, or null if
     * there were none. This is only filled in if the flag
     * {@link PackageManager#GET_ACTIVITIES} was set.
     */
    public ActivityInfo[] activities;

    /**
     * Array of all {@link android.R.styleable#AndroidManifestReceiver
     * &lt;receiver&gt;} tags included under &lt;application&gt;, or null if
     * there were none. This is only filled in if the flag
     * {@link PackageManager#GET_RECEIVERS} was set.
     */
    public ActivityInfo[] receivers;

    /**
     * Array of all {@link android.R.styleable#AndroidManifestService
     * &lt;service&gt;} tags included under &lt;application&gt;, or null if
     * there were none. This is only filled in if the flag
     * {@link PackageManager#GET_SERVICES} was set.
     */
    public ServiceInfo[] services;

    /**
     * Array of all {@link android.R.styleable#AndroidManifestProvider
     * &lt;provider&gt;} tags included under &lt;application&gt;, or null if
     * there were none. This is only filled in if the flag
     * {@link PackageManager#GET_PROVIDERS} was set.
     */
    public ProviderInfo[] providers;

    /**
     * Array of all {@link android.R.styleable#AndroidManifestInstrumentation
     * &lt;instrumentation&gt;} tags included under &lt;manifest&gt;, or null if
     * there were none. This is only filled in if the flag
     * {@link PackageManager#GET_INSTRUMENTATION} was set.
     */
    public InstrumentationInfo[] instrumentation;

    /**
     * Array of all {@link android.R.styleable#AndroidManifestPermission
     * &lt;permission&gt;} tags included under &lt;manifest&gt;, or null if
     * there were none. This is only filled in if the flag
     * {@link PackageManager#GET_PERMISSIONS} was set.
     */
    public PermissionInfo[] permissions;

    /**
     * Array of all {@link android.R.styleable#AndroidManifestUsesPermission
     * &lt;uses-permission&gt;} tags included under &lt;manifest&gt;, or null if
     * there were none. This is only filled in if the flag
     * {@link PackageManager#GET_PERMISSIONS} was set. This list includes all
     * permissions requested, even those that were not granted or known by the
     * system at install time.
     */
    public String[] requestedPermissions;

    /**
     * Array of flags of all
     * {@link android.R.styleable#AndroidManifestUsesPermission
     * &lt;uses-permission&gt;} tags included under &lt;manifest&gt;, or null if
     * there were none. This is only filled in if the flag
     * {@link PackageManager#GET_PERMISSIONS} was set. Each value matches the
     * corresponding entry in {@link #requestedPermissions}, and will have the
     * flags {@link #REQUESTED_PERMISSION_REQUIRED} and
     * {@link #REQUESTED_PERMISSION_GRANTED} set as appropriate.
     */
    public int[] requestedPermissionsFlags;

    /**
     * Flag for {@link #requestedPermissionsFlags}: the requested permission is
     * required for the application to run; the user can not optionally disable
     * it. Currently all permissions are required.
     */
    public static final int REQUESTED_PERMISSION_REQUIRED = 1 << 0;

    /**
     * Flag for {@link #requestedPermissionsFlags}: the requested permission is
     * currently granted to the application.
     */
    public static final int REQUESTED_PERMISSION_GRANTED = 1 << 1;

    /**
     * Array of all signatures read from the package file. This is only filled
     * in if the flag {@link PackageManager#GET_SIGNATURES} was set.
     */
    public Signature[] signatures;

    /**
     * Application specified preferred configuration
     * {@link android.R.styleable#AndroidManifestUsesConfiguration
     * &lt;uses-configuration&gt;} tags included under &lt;manifest&gt;, or null
     * if there were none. This is only filled in if the flag
     * {@link PackageManager#GET_CONFIGURATIONS} was set.
     */
    public ConfigurationInfo[] configPreferences;

    /**
     * The features that this application has said it requires.
     */
    public FeatureInfo[] reqFeatures;

    /**
     * Constant corresponding to <code>auto</code> in the
     * {@link android.R.attr#installLocation} attribute.
     *
     * @hide
     */
    public static final int INSTALL_LOCATION_UNSPECIFIED = -1;
    /**
     * Constant corresponding to <code>auto</code> in the
     * {@link android.R.attr#installLocation} attribute.
     *
     * @hide
     */
    public static final int INSTALL_LOCATION_AUTO = 0;
    /**
     * Constant corresponding to <code>internalOnly</code> in the
     * {@link android.R.attr#installLocation} attribute.
     *
     * @hide
     */
    public static final int INSTALL_LOCATION_INTERNAL_ONLY = 1;
    /**
     * Constant corresponding to <code>preferExternal</code> in the
     * {@link android.R.attr#installLocation} attribute.
     *
     * @hide
     */
    public static final int INSTALL_LOCATION_PREFER_EXTERNAL = 2;
    /**
     * The install location requested by the activity. From the
     * {@link android.R.attr#installLocation} attribute, one of
     * {@link #INSTALL_LOCATION_AUTO}, {@link #INSTALL_LOCATION_INTERNAL_ONLY},
     * {@link #INSTALL_LOCATION_PREFER_EXTERNAL}
     *
     * @hide
     */
    public int installLocation = INSTALL_LOCATION_INTERNAL_ONLY;

    /** @hide */
    public boolean requiredForAllUsers;

    /** @hide */
    public String restrictedAccountType;

    /** @hide */
    public String requiredAccountType;

    public PackageInfo() {
    }

    public String toString() {
        return "PackageInfo{"
                + Integer.toHexString(System.identityHashCode(this)) + " "
                + packageName + "}";
    }

    public int describeContents() {
        return 0;
    }

}
