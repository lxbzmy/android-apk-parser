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

package com.broadwave.android.util;

import java.io.File;
import java.io.IOException;

import com.broadwave.android.android.content.pm.PackageParser;
import com.broadwave.android.android.content.pm.PackageUserState;
import com.broadwave.android.content.pm.ApplicationInfo;
import com.broadwave.android.content.pm.PackageInfo;

/**
 * APK静态解析工具，除了AndroidManifest.xml中的元信息外，还读取本地化的程序名称和最高解析度的图标。
 *
 * @author lxb
 */
public class PackageParserUtil {

    /**
     * 解析APK的元信息。
     *
     * @param apk
     * @return 解析好的VO
     * @throws NameNotFoundException
     */
    public PackageInfo parsePackageInfo(File apk) {

        PackageParser packageParser = null;
        try {
            packageParser = new PackageParser(apk.getCanonicalPath());
            PackageParser.Package info0;
            try {
                info0 = packageParser.parsePackage(apk, null, null,
                        PackageParser.PARSE_MUST_BE_APK);
            } catch (RuntimeException e) {
                throw new ParseException("Package parse exception", e);
            }

            // basic attribute from manifest tag
            com.broadwave.android.android.content.pm.PackageInfo info = PackageParser
                    .generatePackageInfo(info0, null, 0, 1, 1, null,
                            new PackageUserState());

            PackageInfo target = new PackageInfo();
            target.packageName = info.packageName;
            target.versionCode = info.versionCode;
            target.versionName = info.versionName;
            ApplicationInfo ai = new ApplicationInfo();
            target.applicationInfo = ai;
            ai.theme = info.applicationInfo.theme;
            ai.className = info.applicationInfo.className;
            ai.enabled = info.applicationInfo.enabled;
            ai.flags = info.applicationInfo.flags;
            ai.icon = info.applicationInfo.icon;
            ai.labelRes = info.applicationInfo.labelRes;
            ai.name = info.applicationInfo.name;
            ai.nonLocalizedLabel = info.applicationInfo.nonLocalizedLabel;
            ai.packageName = info.packageName;
            ai.iconImage = packageParser.getResource().getDrawable(ai.icon);
            if (ai.labelRes > 0) {
                ai.localizedLabel = packageParser.getResource().getText(
                        ai.labelRes);
            } else {
                ai.localizedLabel = ai.nonLocalizedLabel;
            }
            return target;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (packageParser != null) {
                try {
                    packageParser.close();
                } catch (Exception e) {
                }
            }
        }
        return null;

    }

}
