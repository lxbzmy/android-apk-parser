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

import com.broadwave.android.android.content.res.XmlResourceParser;

import com.broadwave.android.android.os.Bundle;

import java.text.Collator;
import java.util.Comparator;

/**
 * Base class containing information common to all package items held by
 * the package manager.  This provides a very common basic set of attributes:
 * a label, icon, and meta-data.  This class is not intended
 * to be used by itself; it is simply here to share common definitions
 * between all items returned by the package manager.  As such, it does not
 * itself implement Parcelable, but does provide convenience methods to assist
 * in the implementation of Parcelable in subclasses.
 */
public class PackageItemInfo {
    /**
     * Public name of this item. From the "android:name" attribute.
     */
    public String name;

    /**
     * Name of the package that this item is in.
     */
    public String packageName;

    /**
     * A string resource identifier (in the package's resources) of this
     * component's label.  From the "label" attribute or, if not set, 0.
     */
    public int labelRes;

    /**
     * The string provided in the AndroidManifest file, if any.  You
     * probably don't want to use this.  You probably want
     * {@link PackageManager#getApplicationLabel}
     */
    public CharSequence nonLocalizedLabel;

    /**
     * A drawable resource identifier (in the package's resources) of this
     * component's icon.  From the "icon" attribute or, if not set, 0.
     */
    public int icon;

    /**
     * A drawable resource identifier (in the package's resources) of this
     * component's logo. Logos may be larger/wider than icons and are
     * displayed by certain UI elements in place of a name or name/icon
     * combination. From the "logo" attribute or, if not set, 0.
     */
    public int logo;

    /**
     * Additional meta-data associated with this component.  This field
     * will only be filled in if you set the
     * {@link PackageManager#GET_META_DATA} flag when requesting the info.
     */
    public Bundle metaData;

    public PackageItemInfo() {
    }

    public PackageItemInfo(PackageItemInfo orig) {
        name = orig.name;
        if (name != null) name = name.trim();
        packageName = orig.packageName;
        labelRes = orig.labelRes;
        nonLocalizedLabel = orig.nonLocalizedLabel;
        if (nonLocalizedLabel != null) nonLocalizedLabel = nonLocalizedLabel.toString().trim();
        icon = orig.icon;
        logo = orig.logo;
        metaData = orig.metaData;
    }

    /**
     * Get the ApplicationInfo for the application to which this item belongs,
     * if available, otherwise returns null.
     *
     * @return Returns the ApplicationInfo of this item, or null if not known.
     *
     * @hide
     */
    protected ApplicationInfo getApplicationInfo() {
        return null;
    }
}
