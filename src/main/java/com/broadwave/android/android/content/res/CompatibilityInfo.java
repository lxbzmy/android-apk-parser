/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.broadwave.android.android.content.res;

import com.broadwave.android.android.content.pm.ApplicationInfo;
import com.broadwave.android.android.util.DisplayMetrics;

/**
 * CompatibilityInfo class keeps the information about compatibility mode that
 * the application is running under.
 *
 * {@hide}
 */
public class CompatibilityInfo  {
    /** default compatibility info object for compatible applications */
    public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo() {
    };

    /**
     * This is the number of pixels we would like to have along the short axis
     * of an app that needs to run on a normal size screen.
     */
    public static final int DEFAULT_NORMAL_SHORT_DIMENSION = 320;

    /**
     * This is the maximum aspect ratio we will allow while keeping applications
     * in a compatible screen size.
     */
    public static final float MAXIMUM_ASPECT_RATIO = (854f / 480f);

    /**
     * A compatibility flags
     */
    private final int mCompatibilityFlags;

    /**
     * A flag mask to tell if the application needs scaling (when
     * mApplicationScale != 1.0f) {@see compatibilityFlag}
     */
    private static final int SCALING_REQUIRED = 1;

    /**
     * Application must always run in compatibility mode?
     */
    private static final int ALWAYS_NEEDS_COMPAT = 2;

    /**
     * Application never should run in compatibility mode?
     */
    private static final int NEVER_NEEDS_COMPAT = 4;

    /**
     * Set if the application needs to run in screen size compatibility mode.
     */
    private static final int NEEDS_SCREEN_COMPAT = 8;

    /**
     * The effective screen density we have selected for this application.
     */
    public final int applicationDensity;

    /**
     * Application's scale.
     */
    public final float applicationScale;

    /**
     * Application's inverted scale.
     */
    public final float applicationInvertedScale;

    public CompatibilityInfo(ApplicationInfo appInfo, int screenLayout, int sw,
            boolean forceCompat) {
        int compatFlags = 0;

        if (appInfo.requiresSmallestWidthDp != 0
                || appInfo.compatibleWidthLimitDp != 0
                || appInfo.largestWidthLimitDp != 0) {
            // New style screen requirements spec.
            int required = appInfo.requiresSmallestWidthDp != 0 ? appInfo.requiresSmallestWidthDp
                    : appInfo.compatibleWidthLimitDp;
            if (required == 0) {
                required = appInfo.largestWidthLimitDp;
            }
            int compat = appInfo.compatibleWidthLimitDp != 0 ? appInfo.compatibleWidthLimitDp
                    : required;
            if (compat < required) {
                compat = required;
            }
            int largest = appInfo.largestWidthLimitDp;

            if (required > DEFAULT_NORMAL_SHORT_DIMENSION) {
                // For now -- if they require a size larger than the only
                // size we can do in compatibility mode, then don't ever
                // allow the app to go in to compat mode. Trying to run
                // it at a smaller size it can handle will make it far more
                // broken than running at a larger size than it wants or
                // thinks it can handle.
                compatFlags |= NEVER_NEEDS_COMPAT;
            } else if (largest != 0 && sw > largest) {
                // If the screen size is larger than the largest size the
                // app thinks it can work with, then always force it in to
                // compatibility mode.
                compatFlags |= NEEDS_SCREEN_COMPAT | ALWAYS_NEEDS_COMPAT;
            } else if (compat >= sw) {
                // The screen size is something the app says it was designed
                // for, so never do compatibility mode.
                compatFlags |= NEVER_NEEDS_COMPAT;
            } else if (forceCompat) {
                // The app may work better with or without compatibility mode.
                // Let the user decide.
                compatFlags |= NEEDS_SCREEN_COMPAT;
            }

            // Modern apps always support densities.
            applicationDensity = DisplayMetrics.DENSITY_DEVICE;
            applicationScale = 1.0f;
            applicationInvertedScale = 1.0f;

        } else {
            /**
             * Has the application said that its UI is expandable? Based on the
             * <supports-screen> android:expandible in the manifest.
             */
            final int EXPANDABLE = 2;

            /**
             * Has the application said that its UI supports large screens?
             * Based on the <supports-screen> android:largeScreens in the
             * manifest.
             */
            final int LARGE_SCREENS = 8;

            /**
             * Has the application said that its UI supports xlarge screens?
             * Based on the <supports-screen> android:xlargeScreens in the
             * manifest.
             */
            final int XLARGE_SCREENS = 32;

            int sizeInfo = 0;

            // We can't rely on the application always setting
            // FLAG_RESIZEABLE_FOR_SCREENS so will compute it based on various
            // input.
            boolean anyResizeable = false;

            if ((appInfo.flags & ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS) != 0) {
                sizeInfo |= LARGE_SCREENS;
                anyResizeable = true;
                if (!forceCompat) {
                    // If we aren't forcing the app into compatibility mode,
                    // then
                    // assume if it supports large screens that we should allow
                    // it
                    // to use the full space of an xlarge screen as well.
                    sizeInfo |= XLARGE_SCREENS | EXPANDABLE;
                }
            }
            if ((appInfo.flags & ApplicationInfo.FLAG_SUPPORTS_XLARGE_SCREENS) != 0) {
                anyResizeable = true;
                if (!forceCompat) {
                    sizeInfo |= XLARGE_SCREENS | EXPANDABLE;
                }
            }
            if ((appInfo.flags & ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS) != 0) {
                anyResizeable = true;
                sizeInfo |= EXPANDABLE;
            }

            if (forceCompat) {
                // If we are forcing compatibility mode, then ignore an app that
                // just says it is resizable for screens. We'll only have it
                // fill
                // the screen if it explicitly says it supports the screen size
                // we
                // are running in.
                sizeInfo &= ~EXPANDABLE;
            }

            compatFlags |= NEEDS_SCREEN_COMPAT;
            switch (screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                if ((sizeInfo & XLARGE_SCREENS) != 0) {
                    compatFlags &= ~NEEDS_SCREEN_COMPAT;
                }
                if ((appInfo.flags & ApplicationInfo.FLAG_SUPPORTS_XLARGE_SCREENS) != 0) {
                    compatFlags |= NEVER_NEEDS_COMPAT;
                }
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                if ((sizeInfo & LARGE_SCREENS) != 0) {
                    compatFlags &= ~NEEDS_SCREEN_COMPAT;
                }
                if ((appInfo.flags & ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS) != 0) {
                    compatFlags |= NEVER_NEEDS_COMPAT;
                }
                break;
            }

            if ((screenLayout & Configuration.SCREENLAYOUT_COMPAT_NEEDED) != 0) {
                if ((sizeInfo & EXPANDABLE) != 0) {
                    compatFlags &= ~NEEDS_SCREEN_COMPAT;
                } else if (!anyResizeable) {
                    compatFlags |= ALWAYS_NEEDS_COMPAT;
                }
            } else {
                compatFlags &= ~NEEDS_SCREEN_COMPAT;
                compatFlags |= NEVER_NEEDS_COMPAT;
            }

            if ((appInfo.flags & ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES) != 0) {
                applicationDensity = DisplayMetrics.DENSITY_DEVICE;
                applicationScale = 1.0f;
                applicationInvertedScale = 1.0f;
            } else {
                applicationDensity = DisplayMetrics.DENSITY_DEFAULT;
                applicationScale = DisplayMetrics.DENSITY_DEVICE
                        / (float) DisplayMetrics.DENSITY_DEFAULT;
                applicationInvertedScale = 1.0f / applicationScale;
                compatFlags |= SCALING_REQUIRED;
            }
        }

        mCompatibilityFlags = compatFlags;
    }

    private CompatibilityInfo(int compFlags, int dens, float scale,
            float invertedScale) {
        mCompatibilityFlags = compFlags;
        applicationDensity = dens;
        applicationScale = scale;
        applicationInvertedScale = invertedScale;
    }

    private CompatibilityInfo() {
        this(NEVER_NEEDS_COMPAT, DisplayMetrics.DENSITY_DEVICE, 1.0f, 1.0f);
    }

    /**
     * @return true if the scaling is required
     */
    public boolean isScalingRequired() {
        return (mCompatibilityFlags & SCALING_REQUIRED) != 0;
    }

    public boolean supportsScreen() {
        return (mCompatibilityFlags & NEEDS_SCREEN_COMPAT) == 0;
    }

    public boolean neverSupportsScreen() {
        return (mCompatibilityFlags & ALWAYS_NEEDS_COMPAT) != 0;
    }

    public boolean alwaysSupportsScreen() {
        return (mCompatibilityFlags & NEVER_NEEDS_COMPAT) != 0;
    }

    public void applyToConfiguration(int displayDensity,
            Configuration inoutConfig) {
        if (!supportsScreen()) {
            // This is a larger screen device and the app is not
            // compatible with large screens, so we are forcing it to
            // run as if the screen is normal size.
            inoutConfig.screenLayout = (inoutConfig.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK)
                    | Configuration.SCREENLAYOUT_SIZE_NORMAL;
            inoutConfig.screenWidthDp = inoutConfig.compatScreenWidthDp;
            inoutConfig.screenHeightDp = inoutConfig.compatScreenHeightDp;
            inoutConfig.smallestScreenWidthDp = inoutConfig.compatSmallestScreenWidthDp;
        }
        inoutConfig.densityDpi = displayDensity;
        if (isScalingRequired()) {
            float invertedRatio = applicationInvertedScale;
            inoutConfig.densityDpi = (int) ((inoutConfig.densityDpi * invertedRatio) + .5f);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        try {
            CompatibilityInfo oc = (CompatibilityInfo) o;
            if (mCompatibilityFlags != oc.mCompatibilityFlags)
                return false;
            if (applicationDensity != oc.applicationDensity)
                return false;
            if (applicationScale != oc.applicationScale)
                return false;
            if (applicationInvertedScale != oc.applicationInvertedScale)
                return false;
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("{");
        sb.append(applicationDensity);
        sb.append("dpi");
        if (isScalingRequired()) {
            sb.append(" ");
            sb.append(applicationScale);
            sb.append("x");
        }
        if (!supportsScreen()) {
            sb.append(" resizing");
        }
        if (neverSupportsScreen()) {
            sb.append(" never-compat");
        }
        if (alwaysSupportsScreen()) {
            sb.append(" always-compat");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + mCompatibilityFlags;
        result = 31 * result + applicationDensity;
        result = 31 * result + Float.floatToIntBits(applicationScale);
        result = 31 * result + Float.floatToIntBits(applicationInvertedScale);
        return result;
    }

}
