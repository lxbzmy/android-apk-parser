package com.broadwave.android.content.pm;

/**
 * Information you can retrieve about a particular application. This corresponds
 * to information collected from the AndroidManifest.xml's &lt;application&gt;
 * tag.
 */
public class ApplicationInfo extends PackageItemInfo {
    /**
     * Class implementing the Application object. From the "class" attribute.
     */
    public String className;

    /**
     * A style resource identifier (in the package's resources) of the default
     * visual theme of the application. From the "theme" attribute or, if not
     * set, 0.
     */
    public int theme;

    /**
     * Flags associated with the application. Any combination of
     * {@link #FLAG_SYSTEM}, {@link #FLAG_DEBUGGABLE}, {@link #FLAG_HAS_CODE},
     * {@link #FLAG_PERSISTENT}, {@link #FLAG_FACTORY_TEST}, and
     * {@link #FLAG_ALLOW_TASK_REPARENTING} {@link #FLAG_ALLOW_CLEAR_USER_DATA},
     * {@link #FLAG_UPDATED_SYSTEM_APP}, {@link #FLAG_TEST_ONLY},
     * {@link #FLAG_SUPPORTS_SMALL_SCREENS},
     * {@link #FLAG_SUPPORTS_NORMAL_SCREENS},
     * {@link #FLAG_SUPPORTS_LARGE_SCREENS},
     * {@link #FLAG_SUPPORTS_XLARGE_SCREENS},
     * {@link #FLAG_RESIZEABLE_FOR_SCREENS},
     * {@link #FLAG_SUPPORTS_SCREEN_DENSITIES}, {@link #FLAG_VM_SAFE_MODE},
     * {@link #FLAG_INSTALLED}.
     */
    public int flags = 0;

    /**
     * When false, indicates that all components within this application are
     * considered disabled, regardless of their individually set enabled status.
     */
    public boolean enabled = true;

    public String toString() {
        return "ApplicationInfo{"
                + Integer.toHexString(System.identityHashCode(this)) + " "
                + packageName + "}";
    }

}
