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

import java.text.Collator;
import java.util.Comparator;

/**
 * Information you can retrieve about a particular piece of test
 * instrumentation.  This corresponds to information collected
 * from the AndroidManifest.xml's &lt;instrumentation&gt; tag.
 */
public class InstrumentationInfo extends PackageItemInfo implements Parcelable {
    /**
     * The name of the application package being instrumented.  From the
     * "package" attribute.
     */
    public String targetPackage;

    /**
     * Full path to the location of this package.
     */
    public String sourceDir;

    /**
     * Full path to the location of the publicly available parts of this package (i.e. the resources
     * and manifest).  For non-forward-locked apps this will be the same as {@link #sourceDir}.
     */
    public String publicSourceDir;
    /**
     * Full path to a directory assigned to the package for its persistent
     * data.
     */
    public String dataDir;

    /**
     * Full path to the directory where the native JNI libraries are stored.
     *
     * {@hide}
     */
    public String nativeLibraryDir;

    /**
     * Specifies whether or not this instrumentation will handle profiling.
     */
    public boolean handleProfiling;

    /** Specifies whether or not to run this instrumentation as a functional test */
    public boolean functionalTest;

    public InstrumentationInfo() {
    }

    public InstrumentationInfo(InstrumentationInfo orig) {
        super(orig);
        targetPackage = orig.targetPackage;
        sourceDir = orig.sourceDir;
        publicSourceDir = orig.publicSourceDir;
        dataDir = orig.dataDir;
        nativeLibraryDir = orig.nativeLibraryDir;
        handleProfiling = orig.handleProfiling;
        functionalTest = orig.functionalTest;
    }

    public String toString() {
        return "InstrumentationInfo{"
            + Integer.toHexString(System.identityHashCode(this))
            + " " + packageName + "}";
    }

    public int describeContents() {
        return 0;
    }

}
