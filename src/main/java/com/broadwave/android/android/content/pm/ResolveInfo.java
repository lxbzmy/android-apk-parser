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

import com.broadwave.android.android.content.IntentFilter;
import com.broadwave.android.android.os.Parcelable;

import java.text.Collator;
import java.util.Comparator;

/**
 * Information that is returned from resolving an intent
 * against an IntentFilter. This partially corresponds to
 * information collected from the AndroidManifest.xml's
 * &lt;intent&gt; tags.
 */
public class ResolveInfo implements Parcelable {
    /**
     * The activity that corresponds to this resolution match, if this
     * resolution is for an activity.  One and only one of this and
     * serviceInfo must be non-null.
     */
    public ActivityInfo activityInfo;

    /**
     * The service that corresponds to this resolution match, if this
     * resolution is for a service. One and only one of this and
     * activityInfo must be non-null.
     */
    public ServiceInfo serviceInfo;

    /**
     * The IntentFilter that was matched for this ResolveInfo.
     */
    public IntentFilter filter;

    /**
     * The declared priority of this match.  Comes from the "priority"
     * attribute or, if not set, defaults to 0.  Higher values are a higher
     * priority.
     */
    public int priority;

    /**
     * Order of result according to the user's preference.  If the user
     * has not set a preference for this result, the value is 0; higher
     * values are a higher priority.
     */
    public int preferredOrder;

    /**
     * The system's evaluation of how well the activity matches the
     * IntentFilter.  This is a match constant, a combination of
     * {@link IntentFilter#MATCH_CATEGORY_MASK IntentFilter.MATCH_CATEGORY_MASK}
     * and {@link IntentFilter#MATCH_ADJUSTMENT_MASK IntentFiler.MATCH_ADJUSTMENT_MASK}.
     */
    public int match;

    /**
     * Only set when returned by
     * {@link PackageManager#queryIntentActivityOptions}, this tells you
     * which of the given specific intents this result came from.  0 is the
     * first in the list, < 0 means it came from the generic Intent query.
     */
    public int specificIndex = -1;

    /**
     * This filter has specified the Intent.CATEGORY_DEFAULT, meaning it
     * would like to be considered a default action that the user can
     * perform on this data.
     */
    public boolean isDefault;

    /**
     * A string resource identifier (in the package's resources) of this
     * match's label.  From the "label" attribute or, if not set, 0.
     */
    public int labelRes;

    /**
     * The actual string retrieve from <var>labelRes</var> or null if none
     * was provided.
     */
    public CharSequence nonLocalizedLabel;

    /**
     * A drawable resource identifier (in the package's resources) of this
     * match's icon.  From the "icon" attribute or, if not set, 0.
     */
    public int icon;

    /**
     * Optional -- if non-null, the {@link #labelRes} and {@link #icon}
     * resources will be loaded from this package, rather than the one
     * containing the resolved component.
     */
    public String resolvePackageName;

    /**
     * @hide Target comes from system process?
     */
    public boolean system;

    /**
     * Return the icon resource identifier to use for this match.  If the
     * match defines an icon, that is used; else if the activity defines
     * an icon, that is used; else, the application icon is used.
     *
     * @return The icon associated with this match.
     */
    public final int getIconResource() {
        if (icon != 0) return icon;
        if (activityInfo != null) return activityInfo.getIconResource();
        if (serviceInfo != null) return serviceInfo.getIconResource();
        return 0;
    }

    public ResolveInfo() {
    }

    public String toString() {
        ComponentInfo ci = activityInfo != null ? activityInfo : serviceInfo;
        return "ResolveInfo{"
            + Integer.toHexString(System.identityHashCode(this))
            + " " + ci.name + " p=" + priority + " o="
            + preferredOrder + " m=0x" + Integer.toHexString(match) + "}";
    }

    public int describeContents() {
        return 0;
    }
}
