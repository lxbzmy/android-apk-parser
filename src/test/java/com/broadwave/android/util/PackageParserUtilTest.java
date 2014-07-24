package com.broadwave.android.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

import org.junit.Assume;
import org.junit.Test;

import com.broadwave.android.content.pm.PackageInfo;

@SuppressWarnings("javadoc")
public class PackageParserUtilTest {

    @Test
    public void testParseApk1() throws Exception {
        String apk = "/com/broadwave/android/util/bash.apk";
        URL url = getClass().getResource(apk);
        File f = new File(url.toURI());

        PackageParserUtil util = new PackageParserUtil();

        PackageInfo info = util.parsePackageInfo(f);

        assertThat(info.packageName, equalTo("jackpal.androidterm"));
        assertThat(info.versionCode, equalTo(53));
        assertThat(info.versionName, equalTo("1.0.52"));

        assertThat(info.applicationInfo.localizedLabel.toString(),
                equalTo("终端模拟器"));
        assertThat(info.applicationInfo.iconImage.getWidth(null), equalTo(96));

    }

    @Test
    public void testManyPackagesFromDisk() throws Exception {
        String folder = "apk";
        File file = new File(folder);
        Assume.assumeTrue(file.exists());

        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("apk");
            }
        };
        File[] apks = file.listFiles(filter);
        for (File item : apks) {
            System.out.print(item.getName()+":");
            PackageParserUtil util = new PackageParserUtil();
            PackageInfo info = util.parsePackageInfo(item);
            System.out.println(info.packageName + "," + info.versionCode + ","
                    + info.versionName);
        }

    }
}
