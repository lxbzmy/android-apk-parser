package com.broadwave.android.android.content.pm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import javax.imageio.stream.ImageOutputStreamImpl;

import org.junit.Test;

import com.broadwave.android.android.content.pm.PackageParser.Package;
import com.broadwave.android.android.content.res.Configuration;
import com.broadwave.android.android.util.DisplayMetrics;

public class PackageParserTest {

    @Test
    public void testPackageParserBash() throws Exception {

        String apk = "/com/broadwave/android/util/bash.apk";
        URL url = getClass().getResource(apk);
        File f = new File(url.toURI());

        PackageParser packageParser = new PackageParser(apk);
        Package info = packageParser.parsePackage(f, null, null,
                packageParser.PARSE_MUST_BE_APK);

        assertThat(info, notNullValue());
        // basic attribute from manifest tag
        assertThat(info.packageName, equalTo("jackpal.androidterm"));
        assertThat(info.mVersionCode, equalTo(53));
        assertThat(info.mVersionName, equalTo("1.0.52"));

        CharSequence zhLabel = packageParser.getResource().getText(
                info.applicationInfo.labelRes);
        Image drawable = packageParser.getResource().getDrawable(
                info.applicationInfo.icon);
        System.out.println("termial emu icon with=" + drawable.getWidth(null));
        System.out.println(zhLabel);


        packageParser.close();
        // info.applicationInfo.labelRes

    }

    /**
     * <pre>
     * 苹果iPad mini 2
     * 屏幕尺寸：7.9英寸
     * 屏幕分辨率：2048x1536
     * 屏幕像素密度：324PPI
     * 屏幕描述：电容式触摸屏，多点式触摸屏
     * 指取设备：触摸屏
     * 屏幕特性：防指纹涂层，Retina Display技术
     * 支持语言：支持多国语言
     *
     * 三星Galaxy Note Pro P900（WiFi版）参数
     * 屏幕尺寸：12.2英寸
     * 屏幕分辨率：2560x1600
     * 屏幕像素密度：247PPI
     * 屏幕描述：电容式触摸屏，多点式触摸屏
     * 指取设备：触摸屏
     * 屏幕特性：TFT屏幕
     * 支持语言：支持多国语言
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testGetGunCash() throws Exception {

        DisplayMetrics display = new DisplayMetrics();
        display.densityDpi = DisplayMetrics.DENSITY_XHIGH;

        Configuration config = new Configuration();
        config.densityDpi = display.densityDpi;
        config.locale = Locale.CHINA;

        String apk = "/com/broadwave/android/util/gnucash-android.apk";
        URL url = getClass().getResource(apk);
        File f = new File(url.toURI());

        PackageParser packageParser = new PackageParser(apk);
        Package info0 = packageParser.parsePackage(f, null, display,
                packageParser.PARSE_MUST_BE_APK);
        assertThat(info0, notNullValue());
        // basic attribute from manifest tag
        PackageInfo info = packageParser.generatePackageInfo(info0, null, 0, 1,
                1, null, new PackageUserState());

        assertThat(info.packageName, equalTo("org.gnucash.android"));
        assertThat(info.versionCode, equalTo(26));
        assertThat(info.versionName, equalTo("1.3.1"));

        // TODO add locale param
        // 需要构造外部的assetmanager，locale，屏幕尺寸，版本。
        System.out.println(info.applicationInfo.icon);
        System.out.println(info.applicationInfo.labelRes);
        System.out.println(info.applicationInfo.nonLocalizedLabel);

        // TODO read xdip resource.
        packageParser.close();
    }
}
