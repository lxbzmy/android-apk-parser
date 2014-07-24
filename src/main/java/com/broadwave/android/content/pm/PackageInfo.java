package com.broadwave.android.content.pm;


/**
 * Lite static package infor from AOSP.
 * @author lxb
 *
 */
public class PackageInfo {

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
     * Information collected from the &lt;application&gt; tag, or null if there
     * was none.
     */
    public ApplicationInfo applicationInfo;
}
