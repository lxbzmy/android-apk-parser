/**
 *  Copyright 2011 Ryszard Wiśniewski <brut.alll@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.broadwave.android.brut.androlib.res;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.broadwave.android.brut.androlib.AndrolibException;
import com.broadwave.android.brut.androlib.err.CantFindFrameworkResException;
import com.broadwave.android.brut.androlib.res.data.ResPackage;
import com.broadwave.android.brut.androlib.res.data.ResTable;
import com.google.common.io.ByteStreams;

/**
 * @author Ryszard Wiśniewski <brut.alll@gmail.com>
 */
final public class AndrolibResources {
    public ResPackage loadFrameworkPkg(ResTable resTable, int id,
            String frameTag) throws AndrolibException {
        return null;

//        File apk = getFrameworkApk(id, frameTag);
//
//        LOGGER.info("Loading resource table from file: " + apk);
//        ResPackage[] pkgs = getResPackagesFromApk(new ExtFile(apk), resTable,
//                true);
//
//        if (pkgs.length != 1) {
//            throw new AndrolibException(
//                    "Arsc files with zero or multiple packages");
//        }
//
//        ResPackage pkg = pkgs[0];
//        if (pkg.getId() != id) {
//            throw new AndrolibException("Expected pkg of id: "
//                    + String.valueOf(id) + ", got: " + pkg.getId());
//        }
//
//        resTable.addPackage(pkg, false);
//        LOGGER.info("Loaded.");
//        return pkg;
    }

//    private ResPackage[] getResPackagesFromApk(ExtFile apkFile,
//            ResTable resTable, boolean keepBroken) throws AndrolibException {
//        try {
//            return ARSCDecoder.decode(
//                    apkFile.getDirectory().getFileInput("resources.arsc"),
//                    false, keepBroken, resTable).getPackages();
//        } catch (DirectoryException ex) {
//            throw new AndrolibException(
//                    "Could not load resources.arsc from file: " + apkFile, ex);
//        }
//    }

    private File getFrameworkDir() throws AndrolibException {
        String path;

        // if a framework path was specified on the command line, use it
        if (sFrameworkFolder != null) {
            path = sFrameworkFolder;
        } else if (System.getProperty("os.name").equals("Mac OS X")) {
            // store in user-home, for Mac OS X
            path = System.getProperty("user.home") + File.separatorChar + "Library/apktool/framework";
        } else {
            path = System.getProperty("user.home") + File.separatorChar + "apktool" + File.separatorChar + "framework";
        }

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                if (sFrameworkFolder != null) {
                    System.out.println("Can't create Framework directory: "
                            + dir);
                }
                throw new AndrolibException("Can't create directory: " + dir);
            }
        }
        return dir;
    }

    public File getFrameworkApk(int id, String frameTag)
            throws AndrolibException {
        File dir = getFrameworkDir();
        File apk;

        if (frameTag != null) {
            apk = new File(dir, String.valueOf(id) + '-' + frameTag + ".apk");
            if (apk.exists()) {
                return apk;
            }
        }

        apk = new File(dir, String.valueOf(id) + ".apk");
        if (apk.exists()) {
            return apk;
        }

        if (id == 1) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = AndrolibResources.class
                        .getResourceAsStream("/brut/androlib/android-framework.jar");
                out = new FileOutputStream(apk);
                ByteStreams.copy(in, out);
                return apk;
            } catch (IOException ex) {
                throw new AndrolibException(ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }

        throw new CantFindFrameworkResException(id);
    }

    // TODO: dirty static hack. I have to refactor decoding mechanisms.
    public static boolean sKeepBroken = false;
    public static String sFrameworkFolder = null;

    private final static Logger LOGGER = Logger
            .getLogger(AndrolibResources.class.getName());

    private String mMinSdkVersion = null;
    private String mMaxSdkVersion = null;
    private String mTargetSdkVersion = null;

    private String mPackageRenamed = null;

}
