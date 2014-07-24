package com.broadwave.android.android.content.pm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.broadwave.android.android.content.IntentFilter;
import com.broadwave.android.android.os.Build;
import com.broadwave.android.android.os.Bundle;
import com.broadwave.android.android.util.AttributeSet;
import com.broadwave.android.android.util.DisplayMetrics;
import com.broadwave.android.android.util.Slog;
import com.broadwave.android.android.util.TypedValue;
import com.broadwave.android.com.android.internal.R;
import com.broadwave.android.com.android.internal.util.XmlUtils;
//import com.broadwave.android.android.content.pm.PackageParser.Activity;
//import com.broadwave.android.android.content.pm.PackageParser.ActivityIntentInfo;
//import com.broadwave.android.android.content.pm.PackageParser.Instrumentation;
import com.broadwave.android.android.content.res.XmlResourceParser;
import com.broadwave.android.android.content.res.AssetManager;
//import com.broadwave.android.android.content.pm.PackageParser;
import com.broadwave.android.android.content.res.Resources;
import com.broadwave.android.android.content.res.TypedArray;
import com.broadwave.android.android.content.res.Configuration;
import com.broadwave.android.android.content.ComponentName;

//import com.broadwave.android.android.content.pm.TypedArray;

/**
 * @author lxb
 *
 */
public class PackageParser {

    /*
     * ==========================================================================
     *
     * copy from PackageParser.
     *
     * ==========================================================================
     */
    private int mParseError = PackageManager.INSTALL_SUCCEEDED;
    private String mArchiveSourcePath;

    public final static int PARSE_IS_SYSTEM = 1 << 0;
    public final static int PARSE_CHATTY = 1 << 1;
    public final static int PARSE_MUST_BE_APK = 1 << 2;
    public final static int PARSE_IGNORE_PROCESSES = 1 << 3;
    public final static int PARSE_FORWARD_LOCK = 1 << 4;
    public final static int PARSE_ON_SDCARD = 1 << 5;
    public final static int PARSE_IS_SYSTEM_DIR = 1 << 6;

    /**
     * +预读取程序的图标和本地化的信息。
     */
    public final static int PARSE_READ_ICON_AND_LABEL = 1 << 99;



    private static final boolean DEBUG_JAR = false;
    private static final boolean DEBUG_PARSER = false;
    private static final boolean DEBUG_BACKUP = false;
    private boolean mOnlyCoreApps = false;
    private static final String TAG = "PackageParser";
    /** File name in an APK for the Android manifest. */
    private static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";

    private static final int PARSE_DEFAULT_INSTALL_LOCATION = PackageInfo.INSTALL_LOCATION_UNSPECIFIED;

    private static final boolean isPackageFilename(String name) {
        return name.endsWith(".apk");
    }

    // for read certifacate.
    private static final Object mSync = new Object();
    private static WeakReference<byte[]> mReadBuffer;



    /** If set to true, we will only allow package files that exactly match
     *  the DTD.  Otherwise, we try to get as much from the package as we
     *  can without failing.  This should normally be set to false, to
     *  support extensions to the DTD in future versions. */
    private static final boolean RIGID_PARSER = false;


    /**
     * @param archiveSourcePath
     *            APK file url.
     */
    public PackageParser(String archiveSourcePath) {
        mArchiveSourcePath = archiveSourcePath;
    }

    public final static class Package {

        public String packageName;

        // For now we only support one application per package.
        public final ApplicationInfo applicationInfo = new ApplicationInfo();

        public final ArrayList<PackageParser.Permission> permissions = new ArrayList<PackageParser.Permission>(
                0);
        public final ArrayList<PackageParser.PermissionGroup> permissionGroups = new ArrayList<PackageParser.PermissionGroup>(
                0);
        public final ArrayList<Activity> activities = new ArrayList<Activity>(0);
        public final ArrayList<Activity> receivers = new ArrayList<Activity>(0);
        public final ArrayList<PackageParser.Provider> providers = new ArrayList<PackageParser.Provider>(0);
        public final ArrayList<PackageParser.Service> services = new ArrayList<PackageParser.Service>(0);
        public final ArrayList<Instrumentation> instrumentation = new ArrayList<Instrumentation>(
                0);

        public final ArrayList<String> requestedPermissions = new ArrayList<String>();
        public final ArrayList<Boolean> requestedPermissionsRequired = new ArrayList<Boolean>();

        public ArrayList<String> protectedBroadcasts;

        public ArrayList<String> libraryNames = null;
        public ArrayList<String> usesLibraries = null;
        public ArrayList<String> usesOptionalLibraries = null;
        public String[] usesLibraryFiles = null;

        public ArrayList<ActivityIntentInfo> preferredActivityFilters = null;

        public ArrayList<String> mOriginalPackages = null;
        public String mRealPackage = null;
        public ArrayList<String> mAdoptPermissions = null;

        // We store the application meta-data independently to avoid multiple
        // unwanted references
        public Bundle mAppMetaData = null;

        // If this is a 3rd party app, this is the path of the zip file.
        public String mPath;

        // The version code declared for this package.
        public int mVersionCode;

        // The version name declared for this package.
        public String mVersionName;

        // The shared user id that this package wants to use.
        public String mSharedUserId;

        // The shared user label that this package wants to use.
        public int mSharedUserLabel;

        // Signatures that were read from the package.
        public Signature mSignatures[];

        // For use by package manager service for quick lookup of
        // preferred up order.
        public int mPreferredOrder = 0;

        // For use by the package manager to keep track of the path to the
        // file an app came from.
        public String mScanPath;

        // For use by package manager to keep track of where it has done dexopt.
        public boolean mDidDexOpt;

        // // User set enabled state.
        // public int mSetEnabled =
        // PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        //
        // // Whether the package has been stopped.
        // public boolean mSetStopped = false;

        // Additional data supplied by callers.
        public Object mExtras;

        // Whether an operation is currently pending on this package
        public boolean mOperationPending;

        /*
         * Applications hardware preferences
         */
        public final ArrayList<ConfigurationInfo> configPreferences = new ArrayList<ConfigurationInfo>();

        /*
         * Applications requested features
         */
        public ArrayList<FeatureInfo> reqFeatures = null;

        public int installLocation;

        /*
         * An app that's required for all users and cannot be uninstalled for a
         * user
         */
        public boolean mRequiredForAllUsers;

        /*
         * The restricted account authenticator type that is used by this
         * application
         */
        public String mRestrictedAccountType;

        /*
         * The required account type without which this application will not
         * function
         */
        public String mRequiredAccountType;

        /**
         * Digest suitable for comparing whether this package's manifest is the
         * same as another.
         */
        public ManifestDigest manifestDigest;

        /**
         * Data used to feed the KeySetManager
         */
        public Set<PublicKey> mSigningKeys;
        public Map<String, Set<PublicKey>> mKeySetMapping;

        // Whether the package has been stopped.
        public boolean mSetStopped = true;

        // User set enabled state.
        public int mSetEnabled = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;

        public Package(String _name) {
            packageName = _name;
            applicationInfo.packageName = _name;
            applicationInfo.uid = -1;
        }

        public void setPackageName(String newName) {
            packageName = newName;
            applicationInfo.packageName = newName;
            for (int i = permissions.size() - 1; i >= 0; i--) {
                permissions.get(i).setPackageName(newName);
            }
            for (int i = permissionGroups.size() - 1; i >= 0; i--) {
                permissionGroups.get(i).setPackageName(newName);
            }
            for (int i = activities.size() - 1; i >= 0; i--) {
                activities.get(i).setPackageName(newName);
            }
            for (int i = receivers.size() - 1; i >= 0; i--) {
                receivers.get(i).setPackageName(newName);
            }
            for (int i = providers.size() - 1; i >= 0; i--) {
                providers.get(i).setPackageName(newName);
            }
            for (int i = services.size() - 1; i >= 0; i--) {
                services.get(i).setPackageName(newName);
            }
            for (int i = instrumentation.size() - 1; i >= 0; i--) {
                instrumentation.get(i).setPackageName(newName);
            }
        }

        public boolean hasComponentClassName(String name) {
            for (int i = activities.size() - 1; i >= 0; i--) {
                if (name.equals(activities.get(i).className)) {
                    return true;
                }
            }
            for (int i = receivers.size() - 1; i >= 0; i--) {
                if (name.equals(receivers.get(i).className)) {
                    return true;
                }
            }
            for (int i = providers.size() - 1; i >= 0; i--) {
                if (name.equals(providers.get(i).className)) {
                    return true;
                }
            }
            for (int i = services.size() - 1; i >= 0; i--) {
                if (name.equals(services.get(i).className)) {
                    return true;
                }
            }
            for (int i = instrumentation.size() - 1; i >= 0; i--) {
                if (name.equals(instrumentation.get(i).className)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return "Package{"
                    + Integer.toHexString(System.identityHashCode(this)) + " "
                    + packageName + "}";
        }
    }

    /*
     * ==========================================================================
     * Patched code.
     *
     * ==========================================================================
     */

    public final static class Permission extends PackageParser.Component<PackageParser.IntentInfo> {
        public final PermissionInfo info;
        public boolean tree;
        public PackageParser.PermissionGroup group;

        public Permission(PackageParser.Package _owner) {
            super(_owner);
            info = new PermissionInfo();
        }

        public Permission(PackageParser.Package _owner, PermissionInfo _info) {
            super(_owner);
            info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            return "Permission{"
                + Integer.toHexString(System.identityHashCode(this))
                + " " + info.name + "}";
        }
    }

    public final static class PermissionGroup extends PackageParser.Component<PackageParser.IntentInfo> {
        public final PermissionGroupInfo info;

        public PermissionGroup(PackageParser.Package _owner) {
            super(_owner);
            info = new PermissionGroupInfo();
        }

        public PermissionGroup(PackageParser.Package _owner, PermissionGroupInfo _info) {
            super(_owner);
            info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            return "PermissionGroup{"
                + Integer.toHexString(System.identityHashCode(this))
                + " " + info.name + "}";
        }
    }

    /** @hide */
    public static class NewPermissionInfo {
        public final String name;
        public final int sdkVersion;
        public final int fileVersion;

        public NewPermissionInfo(String name, int sdkVersion, int fileVersion) {
            this.name = name;
            this.sdkVersion = sdkVersion;
            this.fileVersion = fileVersion;
        }
    }

    /** @hide */
    public static class SplitPermissionInfo {
        public final String rootPerm;
        public final String[] newPerms;
        public final int targetSdk;

        public SplitPermissionInfo(String rootPerm, String[] newPerms, int targetSdk) {
            this.rootPerm = rootPerm;
            this.newPerms = newPerms;
            this.targetSdk = targetSdk;
        }
    }

    public final static class Activity extends PackageParser.Component<PackageParser.ActivityIntentInfo> {
        public final ActivityInfo info;

        public Activity(final PackageParser.ParseComponentArgs args, final ActivityInfo _info) {
            super(args, _info);
            info = _info;
            info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Activity{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public final static class Service extends PackageParser.Component<PackageParser.ServiceIntentInfo> {
        public final ServiceInfo info;

        public Service(final PackageParser.ParseComponentArgs args, final ServiceInfo _info) {
            super(args, _info);
            info = _info;
            info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Service{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public final static class Provider extends PackageParser.Component<PackageParser.ProviderIntentInfo> {
        public final ProviderInfo info;
        public boolean syncable;

        public Provider(final PackageParser.ParseComponentArgs args, final ProviderInfo _info) {
            super(args, _info);
            info = _info;
            info.applicationInfo = args.owner.applicationInfo;
            syncable = false;
        }

        public Provider(Provider existingProvider) {
            super(existingProvider);
            this.info = existingProvider.info;
            this.syncable = existingProvider.syncable;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Provider{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public final static class Instrumentation extends PackageParser.Component {
        public final InstrumentationInfo info;

        public Instrumentation(final PackageParser.ParsePackageItemArgs args, final InstrumentationInfo _info) {
            super(args, _info);
            info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Instrumentation{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public final static class ActivityIntentInfo extends PackageParser.IntentInfo {
        public final PackageParser.Activity activity;

        public ActivityIntentInfo(PackageParser.Activity _activity) {
            activity = _activity;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ActivityIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            activity.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class IntentInfo extends IntentFilter {
        public boolean hasDefault;
        public int labelRes;
        public CharSequence nonLocalizedLabel;
        public int icon;
        public int logo;
        public int preferred;
    }

    public final static class ServiceIntentInfo extends IntentInfo {
        public final PackageParser.Service service;

        public ServiceIntentInfo(PackageParser.Service _service) {
            service = _service;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ServiceIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            service.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class ProviderIntentInfo extends IntentInfo {
        public final PackageParser.Provider provider;

        public ProviderIntentInfo(PackageParser.Provider provider) {
            this.provider = provider;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ProviderIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            provider.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class Component<II extends IntentInfo> {
        public final PackageParser.Package owner;
        public final ArrayList<II> intents;
        public final String className;
        public Bundle metaData;

        public ComponentName componentName;
        public String componentShortName;

        public Component(PackageParser.Package _owner) {
            owner = _owner;
            intents = null;
            className = null;
        }

        public Component(final PackageParser.ParsePackageItemArgs args, final PackageItemInfo outInfo) {
            owner = args.owner;
            intents = new ArrayList<II>(0);
            String name = args.sa.getNonConfigurationString(args.nameRes, 0);
            if (name == null) {
                className = null;
                args.outError[0] = args.tag + " does not specify android:name";
                return;
            }

//            outInfo.name
//                = PackageParser.buildClassName(owner.applicationInfo.packageName, name, args.outError);
            if (outInfo.name == null) {
                className = null;
                args.outError[0] = args.tag + " does not have valid android:name";
                return;
            }

            className = outInfo.name;

            int iconVal = args.sa.getResourceId(args.iconRes, 0);
            if (iconVal != 0) {
                outInfo.icon = iconVal;
                outInfo.nonLocalizedLabel = null;
            }

            int logoVal = args.sa.getResourceId(args.logoRes, 0);
            if (logoVal != 0) {
                outInfo.logo = logoVal;
            }

            TypedValue v = args.sa.peekValue(args.labelRes);
            if (v != null && (outInfo.labelRes=v.resourceId) == 0) {
                outInfo.nonLocalizedLabel = v.coerceToString();
            }

            outInfo.packageName = owner.packageName;
        }

        public Component(final PackageParser.ParseComponentArgs args, final ComponentInfo outInfo) {
            this(args, (PackageItemInfo)outInfo);
            if (args.outError[0] != null) {
                return;
            }

            if (args.processRes != 0) {
                CharSequence pname;
                if (owner.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.FROYO) {
                    pname = args.sa.getNonConfigurationString(args.processRes,
                            Configuration.NATIVE_CONFIG_VERSION);
                } else {
                    // Some older apps have been seen to use a resource reference
                    // here that on older builds was ignored (with a warning).  We
                    // need to continue to do this for them so they don't break.
                    pname = args.sa.getNonResourceString(args.processRes);
                }
//                outInfo.processName = PackageParser.buildProcessName(owner.applicationInfo.packageName,
//                        owner.applicationInfo.processName, pname,
//                        args.flags, args.sepProcesses, args.outError);
            }

            if (args.descriptionRes != 0) {
                outInfo.descriptionRes = args.sa.getResourceId(args.descriptionRes, 0);
            }

            outInfo.enabled = args.sa.getBoolean(args.enabledRes, true);
        }

        public Component(Component<II> clone) {
            owner = clone.owner;
            intents = clone.intents;
            className = clone.className;
            componentName = clone.componentName;
            componentShortName = clone.componentShortName;
        }

        public ComponentName getComponentName() {
            if (componentName != null) {
                return componentName;
            }
            if (className != null) {
                componentName = new ComponentName(owner.applicationInfo.packageName,
                        className);
            }
            return componentName;
        }

        public void appendComponentShortName(StringBuilder sb) {
//            ComponentName.appendShortString(sb, owner.applicationInfo.packageName, className);
        }

        public void printComponentShortName(PrintWriter pw) {
//            ComponentName.printShortString(pw, owner.applicationInfo.packageName, className);
        }

        public void setPackageName(String packageName) {
            componentName = null;
            componentShortName = null;
        }
    }

    public static class ParseComponentArgs extends ParsePackageItemArgs {
        public final String[] sepProcesses;
        public final int processRes;
        public final int descriptionRes;
        public final int enabledRes;
        public int flags;

        public ParseComponentArgs(Package _owner, String[] _outError,
                int _nameRes, int _labelRes, int _iconRes, int _logoRes,
                String[] _sepProcesses, int _processRes,
                int _descriptionRes, int _enabledRes) {
            super(_owner, _outError, _nameRes, _labelRes, _iconRes, _logoRes);
            sepProcesses = _sepProcesses;
            processRes = _processRes;
            descriptionRes = _descriptionRes;
            enabledRes = _enabledRes;
        }
    }

    /* Light weight package info.
     * @hide
     */
    public static class PackageLite {
        public final String packageName;
        public final int versionCode;
        public final int installLocation;
        public final VerifierInfo[] verifiers;

        public PackageLite(String packageName, int versionCode,
                int installLocation, List<VerifierInfo> verifiers) {
            this.packageName = packageName;
            this.versionCode = versionCode;
            this.installLocation = installLocation;
            this.verifiers = verifiers.toArray(new VerifierInfo[verifiers.size()]);
        }
    }

    public static class ParsePackageItemArgs {
        public final Package owner;
        public final String[] outError;
        public final int nameRes;
        public final int labelRes;
        public final int iconRes;
        public final int logoRes;

        public String tag;
        public TypedArray sa;

        public ParsePackageItemArgs(Package _owner, String[] _outError,
                int _nameRes, int _labelRes, int _iconRes, int _logoRes) {
            owner = _owner;
            outError = _outError;
            nameRes = _nameRes;
            labelRes = _labelRes;
            iconRes = _iconRes;
            logoRes = _logoRes;
        }
    }

    public Resources getResource(){
        return this.res;
    }

    /**
     * 解析APK打包好的文件。
     *
     * @param sourceFile
     * @param destCodePath
     * @param metrics
     * @param flags
     * @return APK的元信息
     * @throws IOException
     */
    public Package parsePackage(File sourceFile, String destCodePath,
            DisplayMetrics metrics, int flags) {
        mParseError = PackageManager.INSTALL_SUCCEEDED;

        mArchiveSourcePath = sourceFile.getPath();
        if (!sourceFile.isFile()) {
            // Slog.w(TAG, "Skipping dir: " + mArchiveSourcePath);
            mParseError = PackageManager.INSTALL_PARSE_FAILED_NOT_APK;
            return null;
        }
        if (!isPackageFilename(sourceFile.getName())
                && (flags & PARSE_MUST_BE_APK) != 0) {
            if ((flags & PARSE_IS_SYSTEM) == 0) {
                // We expect to have non-.apk files in the system dir,
                // so don't warn about them.
                // Slog.w(TAG, "Skipping non-package file: " +
                // mArchiveSourcePath);
            }
            mParseError = PackageManager.INSTALL_PARSE_FAILED_NOT_APK;
            return null;
        }

        if (DEBUG_JAR) {
            // Slog.d(TAG, "Scanning package: " + mArchiveSourcePath);
            // TODO logger
        }

        parser = null;
        assmgr = null;
        res = null;
        boolean assetError = true;
        try {
            assmgr = new AssetManager();
            int cookie = assmgr.addAssetPath(mArchiveSourcePath);
            if (cookie != 0) {
                res = new Resources(assmgr, metrics, null);
                assmgr.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, Build.VERSION.RESOURCES_SDK_INT);
                parser = assmgr
                        .openXmlResourceParser(ANDROID_MANIFEST_FILENAME);
                assetError = false;
            } else {
                // Slog.w(TAG, "Failed adding asset path:"+mArchiveSourcePath);
            }
            assetError = false;

        } catch (Exception e) {
            // TODO logger
            e.printStackTrace();
            // Slog.w(TAG, "Unable to read AndroidManifest.xml of "
            // + mArchiveSourcePath, e);
        }
        if (assetError) {
            if (assmgr != null)
                assmgr.close();
            // if(
            mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST;
            return null;
        }
        String[] errorText = new String[1];
        Package pkg = null;
        Exception errorException = null;
        try {
            // XXXX todo: need to figure out correct configuration.
            pkg = parsePackage(res, parser, flags, errorText);
        } catch (Exception e) {
            errorException = e;
            mParseError = PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
        }

        if (pkg == null) {
            // If we are only parsing core apps, then a null with
            // INSTALL_SUCCEEDED
            // just means to skip this app so don't make a fuss about it.
            if (!mOnlyCoreApps
                    || mParseError != PackageManager.INSTALL_SUCCEEDED) {
                if (errorException != null) {
                    Slog.w(TAG, mArchiveSourcePath, errorException);
                } else {
                    Slog.w(TAG,
                            mArchiveSourcePath + " (at "
                                    + parser.getPositionDescription() + "): "
                                    + errorText[0]);
                }
                if (mParseError == PackageManager.INSTALL_SUCCEEDED) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                }
            }
            mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            close();
            return null;
        }

//        if((flags & PARSE_READ_ICON_AND_LABEL) != 0){
//            preloadResource();
//        }

        //close();


        // Set code and resource paths
        pkg.mPath = destCodePath;
        pkg.mScanPath = mArchiveSourcePath;
        // pkg.applicationInfo.sourceDir = destCodePath;
        // pkg.applicationInfo.publicSourceDir = destRes;
        pkg.mSignatures = null;

        return pkg;
    }

    public void close(){
        parser.close();
        this.res = null;
        assmgr.close();
    }
    /**
     * Generate and return the {@link PackageInfo} for a parsed package.
     *
     * @param p
     *            the parsed package.
     * @param flags
     *            indicating which optional information is included.
     */
    public static PackageInfo generatePackageInfo(PackageParser.Package p,
            int gids[], int flags, long firstInstallTime, long lastUpdateTime,
            HashSet<String> grantedPermissions, PackageUserState state) {

        return generatePackageInfo(p, gids, flags, firstInstallTime,
                lastUpdateTime, grantedPermissions, state, 1);
        // return generatePackageInfo(p, gids, flags, firstInstallTime,
        // lastUpdateTime,
        // grantedPermissions, state, UserHandle.getCallingUserId());
    }

    /**
     * @param p
     * @param gids
     * @param flags
     * @param firstInstallTime
     * @param lastUpdateTime
     * @param grantedPermissions
     * @param state
     * @param userId
     * @return 解析出应用的完整资料。
     */
    public static PackageInfo generatePackageInfo(PackageParser.Package p,
            int gids[], int flags, long firstInstallTime, long lastUpdateTime,
            HashSet<String> grantedPermissions, PackageUserState state,
            int userId) {

        // if (!checkUseInstalledOrBlocked(flags, state)) {
        // return null;
        // }
        PackageInfo pi = new PackageInfo();
        pi.packageName = p.packageName;
        pi.versionCode = p.mVersionCode;
        pi.versionName = p.mVersionName;
        pi.sharedUserId = p.mSharedUserId;
        pi.sharedUserLabel = p.mSharedUserLabel;
        // TODO create applicationinfo
        pi.applicationInfo = generateApplicationInfo(p, flags, state, userId);
        pi.installLocation = p.installLocation;
        if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                || (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            pi.requiredForAllUsers = p.mRequiredForAllUsers;
        }
        pi.restrictedAccountType = p.mRestrictedAccountType;
        pi.requiredAccountType = p.mRequiredAccountType;
        pi.firstInstallTime = firstInstallTime;
        pi.lastUpdateTime = lastUpdateTime;
        if ((flags & PackageManager.GET_GIDS) != 0) {
            pi.gids = gids;
        }
        if ((flags & PackageManager.GET_CONFIGURATIONS) != 0) {
            int N = p.configPreferences.size();
            if (N > 0) {
                pi.configPreferences = new ConfigurationInfo[N];
                p.configPreferences.toArray(pi.configPreferences);
            }
            N = p.reqFeatures != null ? p.reqFeatures.size() : 0;
            if (N > 0) {
                pi.reqFeatures = new FeatureInfo[N];
                p.reqFeatures.toArray(pi.reqFeatures);
            }
        }
        // if ((flags&PackageManager.GET_ACTIVITIES) != 0) {
        // int N = p.activities.size();
        // if (N > 0) {
        // if ((flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
        // pi.activities = new ActivityInfo[N];
        // } else {
        // int num = 0;
        // for (int i=0; i<N; i++) {
        // if (p.activities.get(i).info.enabled) num++;
        // }
        // pi.activities = new ActivityInfo[num];
        // }
        // for (int i=0, j=0; i<N; i++) {
        // final Activity activity = p.activities.get(i);
        // if (activity.info.enabled
        // || (flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
        // pi.activities[j++] = generateActivityInfo(p.activities.get(i), flags,
        // state, userId);
        // }
        // }
        // }
        // }
        // if ((flags&PackageManager.GET_RECEIVERS) != 0) {
        // int N = p.receivers.size();
        // if (N > 0) {
        // if ((flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
        // pi.receivers = new ActivityInfo[N];
        // } else {
        // int num = 0;
        // for (int i=0; i<N; i++) {
        // if (p.receivers.get(i).info.enabled) num++;
        // }
        // pi.receivers = new ActivityInfo[num];
        // }
        // for (int i=0, j=0; i<N; i++) {
        // final Activity activity = p.receivers.get(i);
        // if (activity.info.enabled
        // || (flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
        // pi.receivers[j++] = generateActivityInfo(p.receivers.get(i), flags,
        // state, userId);
        // }
        // }
        // }
        // }
        // if ((flags&PackageManager.GET_SERVICES) != 0) {
        // int N = p.services.size();
        // if (N > 0) {
        // if ((flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
        // pi.services = new ServiceInfo[N];
        // } else {
        // int num = 0;
        // for (int i=0; i<N; i++) {
        // if (p.services.get(i).info.enabled) num++;
        // }
        // pi.services = new ServiceInfo[num];
        // }
        // for (int i=0, j=0; i<N; i++) {
        // final Service service = p.services.get(i);
        // if (service.info.enabled
        // || (flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
        // pi.services[j++] = generateServiceInfo(p.services.get(i), flags,
        // state, userId);
        // }
        // }
        // }
        // }
        // if ((flags&PackageManager.GET_PROVIDERS) != 0) {
        // int N = p.providers.size();
        // if (N > 0) {
        // if ((flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
        // pi.providers = new ProviderInfo[N];
        // } else {
        // int num = 0;
        // for (int i=0; i<N; i++) {
        // if (p.providers.get(i).info.enabled) num++;
        // }
        // pi.providers = new ProviderInfo[num];
        // }
        // for (int i=0, j=0; i<N; i++) {
        // final Provider provider = p.providers.get(i);
        // if (provider.info.enabled
        // || (flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
        // pi.providers[j++] = generateProviderInfo(p.providers.get(i), flags,
        // state, userId);
        // }
        // }
        // }
        // }
        // if ((flags&PackageManager.GET_INSTRUMENTATION) != 0) {
        // int N = p.instrumentation.size();
        // if (N > 0) {
        // pi.instrumentation = new InstrumentationInfo[N];
        // for (int i=0; i<N; i++) {
        // pi.instrumentation[i] = generateInstrumentationInfo(
        // p.instrumentation.get(i), flags);
        // }
        // }
        // }
        // if ((flags&PackageManager.GET_PERMISSIONS) != 0) {
        // int N = p.permissions.size();
        // if (N > 0) {
        // pi.permissions = new PermissionInfo[N];
        // for (int i=0; i<N; i++) {
        // pi.permissions[i] = generatePermissionInfo(p.permissions.get(i),
        // flags);
        // }
        // }
        // N = p.requestedPermissions.size();
        // if (N > 0) {
        // pi.requestedPermissions = new String[N];
        // pi.requestedPermissionsFlags = new int[N];
        // for (int i=0; i<N; i++) {
        // final String perm = p.requestedPermissions.get(i);
        // pi.requestedPermissions[i] = perm;
        // if (p.requestedPermissionsRequired.get(i)) {
        // pi.requestedPermissionsFlags[i] |=
        // PackageInfo.REQUESTED_PERMISSION_REQUIRED;
        // }
        // if (grantedPermissions != null && grantedPermissions.contains(perm))
        // {
        // pi.requestedPermissionsFlags[i] |=
        // PackageInfo.REQUESTED_PERMISSION_GRANTED;
        // }
        // }
        // }
        // }
        if ((flags & PackageManager.GET_SIGNATURES) != 0) {
            int N = (p.mSignatures != null) ? p.mSignatures.length : 0;
            if (N > 0) {
                pi.signatures = new Signature[N];
                System.arraycopy(p.mSignatures, 0, pi.signatures, 0, N);
            }
        }
        return pi;
    }

    private static boolean sCompatibilityModeEnabled = true;
    private Resources res;
    private XmlResourceParser parser;
    private AssetManager assmgr;

    private static boolean copyNeeded(int flags, Package p,
            PackageUserState state, Bundle metaData, int userId) {
        if (userId != 0) {
            // We always need to copy for other users, since we need
            // to fix up the uid.
            return true;
        }
        if (state.enabled != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            boolean enabled = state.enabled == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            if (p.applicationInfo.enabled != enabled) {
                return true;
            }
        }
        if (!state.installed || state.blocked) {
            return true;
        }
        if (state.stopped) {
            return true;
        }
        if ((flags & PackageManager.GET_META_DATA) != 0
                && (metaData != null || p.mAppMetaData != null)) {
            return true;
        }
        if ((flags & PackageManager.GET_SHARED_LIBRARY_FILES) != 0
                && p.usesLibraryFiles != null) {
            return true;
        }
        return false;
    }

    private static void updateApplicationInfo(ApplicationInfo ai, int flags,
            PackageUserState state) {
        // CompatibilityMode is global state.
        if (!sCompatibilityModeEnabled) {
            ai.disableCompatibilityMode();
        }
        if (state.installed) {
            ai.flags |= ApplicationInfo.FLAG_INSTALLED;
        } else {
            ai.flags &= ~ApplicationInfo.FLAG_INSTALLED;
        }
        if (state.blocked) {
            ai.flags |= ApplicationInfo.FLAG_BLOCKED;
        } else {
            ai.flags &= ~ApplicationInfo.FLAG_BLOCKED;
        }
        if (state.enabled == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            ai.enabled = true;
        } else if (state.enabled == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            ai.enabled = (flags & PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS) != 0;
        } else if (state.enabled == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                || state.enabled == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
            ai.enabled = false;
        }
        ai.enabledSetting = state.enabled;
    }

    public static ApplicationInfo generateApplicationInfo(Package p, int flags,
            PackageUserState state, int userId) {
        if (p == null)
            return null;
        if (!copyNeeded(flags, p, state, null, userId)
                && ((flags & PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS) == 0 || state.enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED)) {
            // In this case it is safe to directly modify the internal
            // ApplicationInfo state:
            // - CompatibilityMode is global state, so will be the same for
            // every call.
            // - We only come in to here if the app should reported as
            // installed; this is the
            // default state, and we will do a copy otherwise.
            // - The enable state will always be reported the same for the
            // application across
            // calls; the only exception is for the UNTIL_USED mode, and in that
            // case we will
            // be doing a copy.
            updateApplicationInfo(p.applicationInfo, flags, state);
            return p.applicationInfo;
        }

        // Make shallow copy so we can store the metadata/libraries safely
        ApplicationInfo ai = new ApplicationInfo(p.applicationInfo);
        // if (userId != 0) {
        // ai.uid = UserHandle.getUid(userId, ai.uid);
        // ai.dataDir = PackageManager.getDataDirForUser(userId,
        // ai.packageName);
        // }
        if ((flags & PackageManager.GET_META_DATA) != 0) {
            ai.metaData = p.mAppMetaData;
        }
        if ((flags & PackageManager.GET_SHARED_LIBRARY_FILES) != 0) {
            ai.sharedLibraryFiles = p.usesLibraryFiles;
        }
        if (state.stopped) {
            ai.flags |= ApplicationInfo.FLAG_STOPPED;
        } else {
            ai.flags &= ~ApplicationInfo.FLAG_STOPPED;
        }
        updateApplicationInfo(ai, flags, state);
        return ai;
    }

    /**
     * @param pkg
     * @param flags
     * @return
     */
    public boolean collectCertificates(Package pkg, int flags) {
        pkg.mSignatures = null;

        WeakReference<byte[]> readBufferRef;
        byte[] readBuffer = null;
        synchronized (mSync) {
            readBufferRef = mReadBuffer;
            if (readBufferRef != null) {
                mReadBuffer = null;
                readBuffer = readBufferRef.get();
            }
            if (readBuffer == null) {
                readBuffer = new byte[8192];
                readBufferRef = new WeakReference<byte[]>(readBuffer);
            }
        }

        try {
            JarFile jarFile = new JarFile(mArchiveSourcePath);

            Certificate[] certs = null;

            if ((flags & PARSE_IS_SYSTEM) != 0) {
                // If this package comes from the system image, then we
                // can trust it... we'll just use the AndroidManifest.xml
                // to retrieve its signatures, not validating all of the
                // files.
                JarEntry jarEntry = jarFile
                        .getJarEntry(ANDROID_MANIFEST_FILENAME);
                certs = loadCertificates(jarFile, jarEntry, readBuffer);
                if (certs == null) {
                    // // Slog.e(TAG, "Package " + pkg.packageName
                    // + " has no certificates at entry "
                    // + jarEntry.getName() + "; ignoring!");
                    jarFile.close();
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES;
                    return false;
                }
                if (DEBUG_JAR) {
                    // Slog.i(TAG, "File " + mArchiveSourcePath + ": entry=" +
                    // jarEntry
                    // + " certs=" + (certs != null ? certs.length : 0));
                    if (certs != null) {
                        final int N = certs.length;
                        for (int i = 0; i < N; i++) {
                            // Slog.i(TAG, "  Public key: "
                            // + certs[i].getPublicKey().getEncoded()
                            // + " " + certs[i].getPublicKey());
                        }
                    }
                }
            } else {
                Enumeration<JarEntry> entries = jarFile.entries();
                final Manifest manifest = jarFile.getManifest();
                while (entries.hasMoreElements()) {
                    final JarEntry je = entries.nextElement();
                    if (je.isDirectory())
                        continue;

                    final String name = je.getName();

                    if (name.startsWith("META-INF/"))
                        continue;

                    if (ANDROID_MANIFEST_FILENAME.equals(name)) {
                        final Attributes attributes = manifest
                                .getAttributes(name);
                        pkg.manifestDigest = ManifestDigest
                                .fromAttributes(attributes);
                    }

                    final Certificate[] localCerts = loadCertificates(jarFile,
                            je, readBuffer);
                    if (DEBUG_JAR) {
                        // Slog.i(TAG, "File " + mArchiveSourcePath + " entry "
                        // + je.getName()
                        // + ": certs=" + certs + " ("
                        // + (certs != null ? certs.length : 0) + ")");
                    }

                    if (localCerts == null) {
                        // Slog.e(TAG, "Package " + pkg.packageName
                        // + " has no certificates at entry "
                        // + je.getName() + "; ignoring!");
                        jarFile.close();
                        mParseError = PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES;
                        return false;
                    } else if (certs == null) {
                        certs = localCerts;
                    } else {
                        // Ensure all certificates match.
                        for (int i = 0; i < certs.length; i++) {
                            boolean found = false;
                            for (int j = 0; j < localCerts.length; j++) {
                                if (certs[i] != null
                                        && certs[i].equals(localCerts[j])) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found || certs.length != localCerts.length) {
                                // Slog.e(TAG, "Package " + pkg.packageName
                                // + " has mismatched certificates at entry "
                                // + je.getName() + "; ignoring!");
                                jarFile.close();
                                mParseError = PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES;
                                return false;
                            }
                        }
                    }
                }
            }
            jarFile.close();

            synchronized (mSync) {
                mReadBuffer = readBufferRef;
            }

            if (certs != null && certs.length > 0) {
                final int N = certs.length;
                pkg.mSignatures = new Signature[certs.length];
                for (int i = 0; i < N; i++) {
                    pkg.mSignatures[i] = new Signature(certs[i].getEncoded());
                }
            } else {
                // Slog.e(TAG, "Package " + pkg.packageName
                // + " has no certificates; ignoring!");
                mParseError = PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES;
                return false;
            }
        } catch (CertificateEncodingException e) {
            // Slog.w(TAG, "Exception reading " + mArchiveSourcePath, e);
            mParseError = PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING;
            return false;
        } catch (IOException e) {
            // Slog.w(TAG, "Exception reading " + mArchiveSourcePath, e);
            mParseError = PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING;
            return false;
        } catch (RuntimeException e) {
            // Slog.w(TAG, "Exception reading " + mArchiveSourcePath, e);
            mParseError = PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
            return false;
        }

        return true;
    }

    // copy from android-19
    private Certificate[] loadCertificates(JarFile jarFile, JarEntry je,
            byte[] readBuffer) {
        try {
            // We must read the stream for the JarEntry to retrieve
            // its certificates.
            InputStream is = new BufferedInputStream(jarFile.getInputStream(je));
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
                // not using
            }
            is.close();
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
            Slog.w(TAG,
                    "Exception reading " + je.getName() + " in "
                            + jarFile.getName(), e);
        } catch (RuntimeException e) {
            Slog.w(TAG,
                    "Exception reading " + je.getName() + " in "
                            + jarFile.getName(), e);
        }
        return null;
    }

    /**
     * 解析XML中的包名属性
     *
     * @param parser
     * @param attrs
     * @param flags
     * @param outError
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static String parsePackageName(XmlPullParser parser,
            AttributeSet attrs, int flags, String[] outError)
            throws IOException, XmlPullParserException {

        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
            ;
        }

        if (type != XmlPullParser.START_TAG) {
            outError[0] = "No start tag found";
            return null;
        }
        if (DEBUG_PARSER)
            // Slog.v(TAG, "Root element name: '" + parser.getName() + "'");
            if (!parser.getName().equals("manifest")) {
                outError[0] = "No <manifest> tag";
                return null;
            }
        String pkgName = attrs.getAttributeValue(null, "package");
        if (pkgName == null || pkgName.length() == 0) {
            outError[0] = "<manifest> does not specify package";
            return null;
        }
        String nameError = validateName(pkgName, true);
        if (nameError != null && !"android".equals(pkgName)) {
            outError[0] = "<manifest> specifies bad package name \"" + pkgName
                    + "\": " + nameError;
            return null;
        }

        return pkgName.intern();
    }

    /**
     * 验证应用id是否合规。
     *
     * @param name
     * @param requiresSeparator
     * @return
     */
    private static String validateName(String name, boolean requiresSeparator) {
        final int N = name.length();
        boolean hasSep = false;
        boolean front = true;
        for (int i = 0; i < N; i++) {
            final char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                front = false;
                continue;
            }
            if (!front) {
                if ((c >= '0' && c <= '9') || c == '_') {
                    continue;
                }
            }
            if (c == '.') {
                hasSep = true;
                front = true;
                continue;
            }
            return "bad character '" + c + "'";
        }
        return hasSep || !requiresSeparator ? null
                : "must have at least one '.' separator";
    }

    private Package parsePackage(Resources res, XmlResourceParser parser,
            int flags, String[] outError) throws XmlPullParserException,
            IOException {
        AttributeSet attrs = parser;

        // mParseInstrumentationArgs = null;
        // mParseActivityArgs = null;
        // mParseServiceArgs = null;
        // mParseProviderArgs = null;

        String pkgName = parsePackageName(parser, attrs, flags, outError);
        if (pkgName == null) {
            mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return null;
        }
        int type;

        final Package pkg = new Package(pkgName);
        boolean foundApp = false;

        // manifest tag's attribute.
        String[] manifestAttrs = { "versionCode", "versionName",
                "sharedUserId", "sharedUserLabel", "installLocation" };

//        String[] values = obtainAttributes(attrs, manifestAttrs);

        TypedArray sa = res
                .obtainAttributes(attrs, R.styleable.AndroidManifest);

        pkg.mVersionCode = sa.getInteger(
                R.styleable.AndroidManifest_versionCode, 0);
        pkg.mVersionName = sa.getNonConfigurationString(
                R.styleable.AndroidManifest_versionName, 0);
        if (pkg.mVersionName != null) {
            pkg.mVersionName = pkg.mVersionName.intern();
        }
        String str = sa.getNonConfigurationString(
                R.styleable.AndroidManifest_sharedUserId, 0);
        if (str != null && str.length() > 0) {
            String nameError = validateName(str, true);
            if (nameError != null && !"android".equals(pkgName)) {
                outError[0] = "<manifest> specifies bad sharedUserId name \""
                        + str + "\": " + nameError;
                mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
                return null;
            }
            pkg.mSharedUserId = str.intern();
            pkg.mSharedUserLabel = sa.getResourceId(
                    R.styleable.AndroidManifest_sharedUserLabel, 0);
        }

        try {
            pkg.installLocation = sa.getInteger(
                    R.styleable.AndroidManifest_installLocation,
                    PARSE_DEFAULT_INSTALL_LOCATION);
             pkg.applicationInfo.installLocation = pkg.installLocation;
        } catch (UnsupportedOperationException e) {
            //个别apk里面这一项值不是整数而是字符串字面量，例如"internalOnly"本来应该是1的。
            pkg.installLocation = PARSE_DEFAULT_INSTALL_LOCATION;
            pkg.applicationInfo.installLocation = pkg.installLocation;
        }

         /* Set the global "forward lock" flag */
         if ((flags & PARSE_FORWARD_LOCK) != 0) {
             pkg.applicationInfo.flags |= ApplicationInfo.FLAG_FORWARD_LOCK;
         }

         /* Set the global "on SD card" flag */
         if ((flags & PARSE_ON_SDCARD) != 0) {
             pkg.applicationInfo.flags |= ApplicationInfo.FLAG_EXTERNAL_STORAGE;
         }


         // Resource boolean are -1, so 1 means we don't know the value.
         int supportsSmallScreens = 1;
         int supportsNormalScreens = 1;
         int supportsLargeScreens = 1;
         int supportsXLargeScreens = 1;
         int resizeable = 1;
         int anyDensity = 1;

         int outerDepth = parser.getDepth();
         while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                 && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
             if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                 continue;
             }
//
             String tagName = parser.getName();
             if (tagName.equals("application")) {
                 if (foundApp) {
                     if (RIGID_PARSER) {
                         outError[0] = "<manifest> has more than one <application>";
                         mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                         return null;
                     } else {
                         Slog.w(TAG, "<manifest> has more than one <application>");
                         XmlUtils.skipCurrentTag(parser);
                         continue;
                     }
                 }

                 foundApp = true;
                 if (!parseApplication(pkg, res, parser, attrs, flags, outError)) {
                     return null;
                 }
             } //else if (tagName.equals("keys")) {
//                 if (!parseKeys(pkg, res, parser, attrs, outError)) {
//                     return null;
//                 }
//             } else if (tagName.equals("permission-group")) {
//                 if (parsePermissionGroup(pkg, flags, res, parser, attrs, outError) == null) {
//                     return null;
//                 }
//             } else if (tagName.equals("permission")) {
//                 if (parsePermission(pkg, res, parser, attrs, outError) == null) {
//                     return null;
//                 }
//             } else if (tagName.equals("permission-tree")) {
//                 if (parsePermissionTree(pkg, res, parser, attrs, outError) == null) {
//                     return null;
//                 }
//             } else if (tagName.equals("uses-permission")) {
//                 if (!parseUsesPermission(pkg, res, parser, attrs, outError)) {
//                     return null;
//                 }
//
//             } else if (tagName.equals("uses-configuration")) {
//                 ConfigurationInfo cPref = new ConfigurationInfo();
//                 sa = res.obtainAttributes(attrs,
//                         com.android.internal.R.styleable.AndroidManifestUsesConfiguration);
//                 cPref.reqTouchScreen = sa.getInt(
//                         com.android.internal.R.styleable.AndroidManifestUsesConfiguration_reqTouchScreen,
//                         Configuration.TOUCHSCREEN_UNDEFINED);
//                 cPref.reqKeyboardType = sa.getInt(
//                         com.android.internal.R.styleable.AndroidManifestUsesConfiguration_reqKeyboardType,
//                         Configuration.KEYBOARD_UNDEFINED);
//                 if (sa.getBoolean(
//                         com.android.internal.R.styleable.AndroidManifestUsesConfiguration_reqHardKeyboard,
//                         false)) {
//                     cPref.reqInputFeatures |= ConfigurationInfo.INPUT_FEATURE_HARD_KEYBOARD;
//                 }
//                 cPref.reqNavigation = sa.getInt(
//                         com.android.internal.R.styleable.AndroidManifestUsesConfiguration_reqNavigation,
//                         Configuration.NAVIGATION_UNDEFINED);
//                 if (sa.getBoolean(
//                         com.android.internal.R.styleable.AndroidManifestUsesConfiguration_reqFiveWayNav,
//                         false)) {
//                     cPref.reqInputFeatures |= ConfigurationInfo.INPUT_FEATURE_FIVE_WAY_NAV;
//                 }
//                 sa.recycle();
//                 pkg.configPreferences.add(cPref);
//
//                 XmlUtils.skipCurrentTag(parser);
//
//             } else if (tagName.equals("uses-feature")) {
//                 FeatureInfo fi = new FeatureInfo();
//                 sa = res.obtainAttributes(attrs,
//                         com.android.internal.R.styleable.AndroidManifestUsesFeature);
//                 // Note: don't allow this value to be a reference to a resource
//                 // that may change.
//                 fi.name = sa.getNonResourceString(
//                         com.android.internal.R.styleable.AndroidManifestUsesFeature_name);
//                 if (fi.name == null) {
//                     fi.reqGlEsVersion = sa.getInt(
//                             com.android.internal.R.styleable.AndroidManifestUsesFeature_glEsVersion,
//                             FeatureInfo.GL_ES_VERSION_UNDEFINED);
//                 }
//                 if (sa.getBoolean(
//                         com.android.internal.R.styleable.AndroidManifestUsesFeature_required,
//                         true)) {
//                     fi.flags |= FeatureInfo.FLAG_REQUIRED;
//                 }
//                 sa.recycle();
//                 if (pkg.reqFeatures == null) {
//                     pkg.reqFeatures = new ArrayList<FeatureInfo>();
//                 }
//                 pkg.reqFeatures.add(fi);
//
//                 if (fi.name == null) {
//                     ConfigurationInfo cPref = new ConfigurationInfo();
//                     cPref.reqGlEsVersion = fi.reqGlEsVersion;
//                     pkg.configPreferences.add(cPref);
//                 }
//
//                 XmlUtils.skipCurrentTag(parser);
//
//             } else if (tagName.equals("uses-sdk")) {
//                 if (SDK_VERSION > 0) {
//                     sa = res.obtainAttributes(attrs,
//                             com.android.internal.R.styleable.AndroidManifestUsesSdk);
//
//                     int minVers = 0;
//                     String minCode = null;
//                     int targetVers = 0;
//                     String targetCode = null;
//
//                     TypedValue val = sa.peekValue(
//                             com.android.internal.R.styleable.AndroidManifestUsesSdk_minSdkVersion);
//                     if (val != null) {
//                         if (val.type == TypedValue.TYPE_STRING && val.string != null) {
//                             targetCode = minCode = val.string.toString();
//                         } else {
//                             // If it's not a string, it's an integer.
//                             targetVers = minVers = val.data;
//                         }
//                     }
//
//                     val = sa.peekValue(
//                             com.android.internal.R.styleable.AndroidManifestUsesSdk_targetSdkVersion);
//                     if (val != null) {
//                         if (val.type == TypedValue.TYPE_STRING && val.string != null) {
//                             targetCode = minCode = val.string.toString();
//                         } else {
//                             // If it's not a string, it's an integer.
//                             targetVers = val.data;
//                         }
//                     }
//
//                     sa.recycle();
//
//                     if (minCode != null) {
//                         if (!minCode.equals(SDK_CODENAME)) {
//                             if (SDK_CODENAME != null) {
//                                 outError[0] = "Requires development platform " + minCode
//                                         + " (current platform is " + SDK_CODENAME + ")";
//                             } else {
//                                 outError[0] = "Requires development platform " + minCode
//                                         + " but this is a release platform.";
//                             }
//                             mParseError = PackageManager.INSTALL_FAILED_OLDER_SDK;
//                             return null;
//                         }
//                     } else if (minVers > SDK_VERSION) {
//                         outError[0] = "Requires newer sdk version #" + minVers
//                                 + " (current version is #" + SDK_VERSION + ")";
//                         mParseError = PackageManager.INSTALL_FAILED_OLDER_SDK;
//                         return null;
//                     }
//
//                     if (targetCode != null) {
//                         if (!targetCode.equals(SDK_CODENAME)) {
//                             if (SDK_CODENAME != null) {
//                                 outError[0] = "Requires development platform " + targetCode
//                                         + " (current platform is " + SDK_CODENAME + ")";
//                             } else {
//                                 outError[0] = "Requires development platform " + targetCode
//                                         + " but this is a release platform.";
//                             }
//                             mParseError = PackageManager.INSTALL_FAILED_OLDER_SDK;
//                             return null;
//                         }
//                         // If the code matches, it definitely targets this SDK.
//                         pkg.applicationInfo.targetSdkVersion
//                                 = android.os.Build.VERSION_CODES.CUR_DEVELOPMENT;
//                     } else {
//                         pkg.applicationInfo.targetSdkVersion = targetVers;
//                     }
//                 }
//
//                 XmlUtils.skipCurrentTag(parser);
//
//             } else if (tagName.equals("supports-screens")) {
//                 sa = res.obtainAttributes(attrs,
//                         com.android.internal.R.styleable.AndroidManifestSupportsScreens);
//
//                 pkg.applicationInfo.requiresSmallestWidthDp = sa.getInteger(
//                         com.android.internal.R.styleable.AndroidManifestSupportsScreens_requiresSmallestWidthDp,
//                         0);
//                 pkg.applicationInfo.compatibleWidthLimitDp = sa.getInteger(
//                         com.android.internal.R.styleable.AndroidManifestSupportsScreens_compatibleWidthLimitDp,
//                         0);
//                 pkg.applicationInfo.largestWidthLimitDp = sa.getInteger(
//                         com.android.internal.R.styleable.AndroidManifestSupportsScreens_largestWidthLimitDp,
//                         0);
//
//                 // This is a trick to get a boolean and still able to detect
//                 // if a value was actually set.
//                 supportsSmallScreens = sa.getInteger(
//                         com.android.internal.R.styleable.AndroidManifestSupportsScreens_smallScreens,
//                         supportsSmallScreens);
//                 supportsNormalScreens = sa.getInteger(
//                         com.android.internal.R.styleable.AndroidManifestSupportsScreens_normalScreens,
//                         supportsNormalScreens);
//                 supportsLargeScreens = sa.getInteger(
//                         com.android.internal.R.styleable.AndroidManifestSupportsScreens_largeScreens,
//                         supportsLargeScreens);
//                 supportsXLargeScreens = sa.getInteger(
//                         com.android.internal.R.styleable.AndroidManifestSupportsScreens_xlargeScreens,
//                         supportsXLargeScreens);
//                 resizeable = sa.getInteger(
//                         com.android.internal.R.styleable.AndroidManifestSupportsScreens_resizeable,
//                         resizeable);
//                 anyDensity = sa.getInteger(
//                         com.android.internal.R.styleable.AndroidManifestSupportsScreens_anyDensity,
//                         anyDensity);
//
//                 sa.recycle();
//
//                 XmlUtils.skipCurrentTag(parser);
//
//             } else if (tagName.equals("protected-broadcast")) {
//                 sa = res.obtainAttributes(attrs,
//                         com.android.internal.R.styleable.AndroidManifestProtectedBroadcast);
//
//                 // Note: don't allow this value to be a reference to a resource
//                 // that may change.
//                 String name = sa.getNonResourceString(
//                         com.android.internal.R.styleable.AndroidManifestProtectedBroadcast_name);
//
//                 sa.recycle();
//
//                 if (name != null && (flags&PARSE_IS_SYSTEM) != 0) {
//                     if (pkg.protectedBroadcasts == null) {
//                         pkg.protectedBroadcasts = new ArrayList<String>();
//                     }
//                     if (!pkg.protectedBroadcasts.contains(name)) {
//                         pkg.protectedBroadcasts.add(name.intern());
//                     }
//                 }
//
//                 XmlUtils.skipCurrentTag(parser);
//
//             } else if (tagName.equals("instrumentation")) {
//                 if (parseInstrumentation(pkg, res, parser, attrs, outError) == null) {
//                     return null;
//                 }
//
//             } else if (tagName.equals("original-package")) {
//                 sa = res.obtainAttributes(attrs,
//                         com.android.internal.R.styleable.AndroidManifestOriginalPackage);
//
//                 String orig =sa.getNonConfigurationString(
//                         com.android.internal.R.styleable.AndroidManifestOriginalPackage_name, 0);
//                 if (!pkg.packageName.equals(orig)) {
//                     if (pkg.mOriginalPackages == null) {
//                         pkg.mOriginalPackages = new ArrayList<String>();
//                         pkg.mRealPackage = pkg.packageName;
//                     }
//                     pkg.mOriginalPackages.add(orig);
//                 }
//
//                 sa.recycle();
//
//                 XmlUtils.skipCurrentTag(parser);
//
//             } else if (tagName.equals("adopt-permissions")) {
//                 sa = res.obtainAttributes(attrs,
//                         com.android.internal.R.styleable.AndroidManifestOriginalPackage);
//
//                 String name = sa.getNonConfigurationString(
//                         com.android.internal.R.styleable.AndroidManifestOriginalPackage_name, 0);
//
//                 sa.recycle();
//
//                 if (name != null) {
//                     if (pkg.mAdoptPermissions == null) {
//                         pkg.mAdoptPermissions = new ArrayList<String>();
//                     }
//                     pkg.mAdoptPermissions.add(name);
//                 }
//
//                 XmlUtils.skipCurrentTag(parser);
//
//             } else if (tagName.equals("uses-gl-texture")) {
//                 // Just skip this tag
//                 XmlUtils.skipCurrentTag(parser);
//                 continue;
//
//             } else if (tagName.equals("compatible-screens")) {
//                 // Just skip this tag
//                 XmlUtils.skipCurrentTag(parser);
//                 continue;
//             } else if (tagName.equals("supports-input")) {
//                 XmlUtils.skipCurrentTag(parser);
//                 continue;
//
//             } else if (tagName.equals("eat-comment")) {
//                 // Just skip this tag
//                 XmlUtils.skipCurrentTag(parser);
//                 continue;
//
//             } else if (RIGID_PARSER) {
//                 outError[0] = "Bad element under <manifest>: "
//                     + parser.getName();
//                 mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
//                 return null;
//
//             } else {
//                 Slog.w(TAG, "Unknown element under <manifest>: " + parser.getName()
//                         + " at " + mArchiveSourcePath + " "
//                         + parser.getPositionDescription());
//                 XmlUtils.skipCurrentTag(parser);
//                 continue;
//             }
         }
//
//         if (!foundApp && pkg.instrumentation.size() == 0) {
//             outError[0] = "<manifest> does not contain an <application> or <instrumentation>";
//             mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
//         }
//
//         final int NP = PackageParser.NEW_PERMISSIONS.length;
//         StringBuilder implicitPerms = null;
//         for (int ip=0; ip<NP; ip++) {
//             final PackageParser.NewPermissionInfo npi
//                     = PackageParser.NEW_PERMISSIONS[ip];
//             if (pkg.applicationInfo.targetSdkVersion >= npi.sdkVersion) {
//                 break;
//             }
//             if (!pkg.requestedPermissions.contains(npi.name)) {
//                 if (implicitPerms == null) {
//                     implicitPerms = new StringBuilder(128);
//                     implicitPerms.append(pkg.packageName);
//                     implicitPerms.append(": compat added ");
//                 } else {
//                     implicitPerms.append(' ');
//                 }
//                 implicitPerms.append(npi.name);
//                 pkg.requestedPermissions.add(npi.name);
//                 pkg.requestedPermissionsRequired.add(Boolean.TRUE);
//             }
//         }
//         if (implicitPerms != null) {
//             Slog.i(TAG, implicitPerms.toString());
//         }
//
//         final int NS = PackageParser.SPLIT_PERMISSIONS.length;
//         for (int is=0; is<NS; is++) {
//             final PackageParser.SplitPermissionInfo spi
//                     = PackageParser.SPLIT_PERMISSIONS[is];
//             if (pkg.applicationInfo.targetSdkVersion >= spi.targetSdk
//                     || !pkg.requestedPermissions.contains(spi.rootPerm)) {
//                 continue;
//             }
//             for (int in=0; in<spi.newPerms.length; in++) {
//                 final String perm = spi.newPerms[in];
//                 if (!pkg.requestedPermissions.contains(perm)) {
//                     pkg.requestedPermissions.add(perm);
//                     pkg.requestedPermissionsRequired.add(Boolean.TRUE);
//                 }
//             }
//         }

//         if (supportsSmallScreens < 0 || (supportsSmallScreens > 0
//                 && pkg.applicationInfo.targetSdkVersion
//                         >= android.os.Build.VERSION_CODES.DONUT)) {
//             pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS;
//         }
//         if (supportsNormalScreens != 0) {
//             pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS;
//         }
//         if (supportsLargeScreens < 0 || (supportsLargeScreens > 0
//                 && pkg.applicationInfo.targetSdkVersion
//                         >= android.os.Build.VERSION_CODES.DONUT)) {
//             pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS;
//         }
//         if (supportsXLargeScreens < 0 || (supportsXLargeScreens > 0
//                 && pkg.applicationInfo.targetSdkVersion
//                         >= android.os.Build.VERSION_CODES.GINGERBREAD)) {
//             pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SUPPORTS_XLARGE_SCREENS;
//         }
//         if (resizeable < 0 || (resizeable > 0
//                 && pkg.applicationInfo.targetSdkVersion
//                         >= android.os.Build.VERSION_CODES.DONUT)) {
//             pkg.applicationInfo.flags |= ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS;
//         }
//         if (anyDensity < 0 || (anyDensity > 0
//                 && pkg.applicationInfo.targetSdkVersion
//                         >= android.os.Build.VERSION_CODES.DONUT)) {
//             pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES;
//         }

         /*
          * b/8528162: Ignore the <uses-permission android:required> attribute if
          * targetSdkVersion < JELLY_BEAN_MR2. There are lots of apps in the wild
          * which are improperly using this attribute, even though it never worked.
          */
         if (pkg.applicationInfo.targetSdkVersion < Build.VERSION_CODES.JELLY_BEAN_MR2) {
             for (int i = 0; i < pkg.requestedPermissionsRequired.size(); i++) {
                 pkg.requestedPermissionsRequired.set(i, Boolean.TRUE);
             }
         }

         return pkg;
    }


    private boolean parseApplication(Package owner, Resources res,
            XmlPullParser parser, AttributeSet attrs, int flags, String[] outError)
        throws XmlPullParserException, IOException {
        final ApplicationInfo ai = owner.applicationInfo;
        final String pkgName = owner.applicationInfo.packageName;

        TypedArray sa = res.obtainAttributes(attrs,
                R.styleable.AndroidManifestApplication);

        String name = sa.getNonConfigurationString(
                R.styleable.AndroidManifestApplication_name, 0);
        if (name != null) {
//            ai.className = buildClassName(pkgName, name, outError);
//            if (ai.className == null) {
//                sa.recycle();
//                mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
//                return false;
//            }
        }

//        String manageSpaceActivity = sa.getNonConfigurationString(
//                R.styleable.AndroidManifestApplication_manageSpaceActivity,
//                Configuration.NATIVE_CONFIG_VERSION);
//        if (manageSpaceActivity != null) {
//            ai.manageSpaceActivityName = buildClassName(pkgName, manageSpaceActivity,
//                    outError);
//        }
//
//        boolean allowBackup = sa.getBoolean(
//                R.styleable.AndroidManifestApplication_allowBackup, true);
//        if (allowBackup) {
//            ai.flags |= ApplicationInfo.FLAG_ALLOW_BACKUP;
//
//            // backupAgent, killAfterRestore, and restoreAnyVersion are only relevant
//            // if backup is possible for the given application.
//            String backupAgent = sa.getNonConfigurationString(
//                    R.styleable.AndroidManifestApplication_backupAgent,
//                    Configuration.NATIVE_CONFIG_VERSION);
//            if (backupAgent != null) {
//                ai.backupAgentName = buildClassName(pkgName, backupAgent, outError);
//                if (DEBUG_BACKUP) {
//                    Slog.v(TAG, "android:backupAgent = " + ai.backupAgentName
//                            + " from " + pkgName + "+" + backupAgent);
//                }
//
//                if (sa.getBoolean(
//                        R.styleable.AndroidManifestApplication_killAfterRestore,
//                        true)) {
//                    ai.flags |= ApplicationInfo.FLAG_KILL_AFTER_RESTORE;
//                }
//                if (sa.getBoolean(
//                        R.styleable.AndroidManifestApplication_restoreAnyVersion,
//                        false)) {
//                    ai.flags |= ApplicationInfo.FLAG_RESTORE_ANY_VERSION;
//                }
//            }
//        }

        TypedValue v = sa.peekValue(
                R.styleable.AndroidManifestApplication_label);
        if (v != null && (ai.labelRes=v.resourceId) == 0) {
            ai.nonLocalizedLabel = v.coerceToString();
        }

        ai.icon = sa.getResourceId(
                R.styleable.AndroidManifestApplication_icon, 0);
        ai.logo = sa.getResourceId(
                R.styleable.AndroidManifestApplication_logo, 0);
        ai.theme = sa.getResourceId(
                R.styleable.AndroidManifestApplication_theme, 0);
        ai.descriptionRes = sa.getResourceId(
                R.styleable.AndroidManifestApplication_description, 0);

        if ((flags&PARSE_IS_SYSTEM) != 0) {
            if (sa.getBoolean(
                    R.styleable.AndroidManifestApplication_persistent,
                    false)) {
                ai.flags |= ApplicationInfo.FLAG_PERSISTENT;
            }
        }

        if (sa.getBoolean(
                R.styleable.AndroidManifestApplication_requiredForAllUsers,
                false)) {
            owner.mRequiredForAllUsers = true;
        }

        String restrictedAccountType = sa.getString(R.styleable
                .AndroidManifestApplication_restrictedAccountType);
        if (restrictedAccountType != null && restrictedAccountType.length() > 0) {
            owner.mRestrictedAccountType = restrictedAccountType;
        }

        String requiredAccountType = sa.getString(R.styleable
                .AndroidManifestApplication_requiredAccountType);
        if (requiredAccountType != null && requiredAccountType.length() > 0) {
            owner.mRequiredAccountType = requiredAccountType;
        }

        if (sa.getBoolean(
                R.styleable.AndroidManifestApplication_debuggable,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_DEBUGGABLE;
        }

        if (sa.getBoolean(
                R.styleable.AndroidManifestApplication_vmSafeMode,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_VM_SAFE_MODE;
        }

        boolean hardwareAccelerated = sa.getBoolean(
                R.styleable.AndroidManifestApplication_hardwareAccelerated,
                owner.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH);

        if (sa.getBoolean(
                R.styleable.AndroidManifestApplication_hasCode,
                true)) {
            ai.flags |= ApplicationInfo.FLAG_HAS_CODE;
        }

        if (sa.getBoolean(
                R.styleable.AndroidManifestApplication_allowTaskReparenting,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_ALLOW_TASK_REPARENTING;
        }

        if (sa.getBoolean(
                R.styleable.AndroidManifestApplication_allowClearUserData,
                true)) {
            ai.flags |= ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA;
        }

        if (sa.getBoolean(
                R.styleable.AndroidManifestApplication_testOnly,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_TEST_ONLY;
        }

        if (sa.getBoolean(
                R.styleable.AndroidManifestApplication_largeHeap,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_LARGE_HEAP;
        }

        if (sa.getBoolean(
                R.styleable.AndroidManifestApplication_supportsRtl,
                false /* default is no RTL support*/)) {
            ai.flags |= ApplicationInfo.FLAG_SUPPORTS_RTL;
        }

        String str;
        str = sa.getNonConfigurationString(
                R.styleable.AndroidManifestApplication_permission, 0);
        ai.permission = (str != null && str.length() > 0) ? str.intern() : null;

        if (owner.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.FROYO) {
            str = sa.getNonConfigurationString(
                    R.styleable.AndroidManifestApplication_taskAffinity,
                    Configuration.NATIVE_CONFIG_VERSION);
        } else {
            // Some older apps have been seen to use a resource reference
            // here that on older builds was ignored (with a warning).  We
            // need to continue to do this for them so they don't break.
            str = sa.getNonResourceString(
                    R.styleable.AndroidManifestApplication_taskAffinity);
        }
//        ai.taskAffinity = buildTaskAffinityName(ai.packageName, ai.packageName,
//                str, outError);

        if (outError[0] == null) {
            CharSequence pname;
            if (owner.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.FROYO) {
                pname = sa.getNonConfigurationString(
                        R.styleable.AndroidManifestApplication_process,
                        Configuration.NATIVE_CONFIG_VERSION);
            } else {
                // Some older apps have been seen to use a resource reference
                // here that on older builds was ignored (with a warning).  We
                // need to continue to do this for them so they don't break.
                pname = sa.getNonResourceString(
                        R.styleable.AndroidManifestApplication_process);
            }
//            ai.processName = buildProcessName(ai.packageName, null, pname,
//                    flags, mSeparateProcesses, outError);

            ai.enabled = sa.getBoolean(
                    R.styleable.AndroidManifestApplication_enabled, true);

//            if (false) {
//                if (sa.getBoolean(
//                        R.styleable.AndroidManifestApplication_cantSaveState,
//                        false)) {
//                    ai.flags |= ApplicationInfo.FLAG_CANT_SAVE_STATE;
//
//                    // A heavy-weight application can not be in a custom process.
//                    // We can do direct compare because we intern all strings.
//                    if (ai.processName != null && ai.processName != ai.packageName) {
//                        outError[0] = "cantSaveState applications can not use custom processes";
//                    }
//                }
//            }
        }

        ai.uiOptions = sa.getInt(
                R.styleable.AndroidManifestApplication_uiOptions, 0);

        sa.recycle();

        if (outError[0] != null) {
            mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }

        final int innerDepth = parser.getDepth();

        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }

            String tagName = parser.getName();
//            if (tagName.equals("activity")) {
//                Activity a = parseActivity(owner, res, parser, attrs, flags, outError, false,
//                        hardwareAccelerated);
//                if (a == null) {
//                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
//                    return false;
//                }
//
//                owner.activities.add(a);
//
//            } else if (tagName.equals("receiver")) {
//                Activity a = parseActivity(owner, res, parser, attrs, flags, outError, true, false);
//                if (a == null) {
//                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
//                    return false;
//                }
//
//                owner.receivers.add(a);
//
//            } else if (tagName.equals("service")) {
//                Service s = parseService(owner, res, parser, attrs, flags, outError);
//                if (s == null) {
//                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
//                    return false;
//                }
//
//                owner.services.add(s);
//
//            } else if (tagName.equals("provider")) {
//                Provider p = parseProvider(owner, res, parser, attrs, flags, outError);
//                if (p == null) {
//                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
//                    return false;
//                }
//
//                owner.providers.add(p);
//
//            } else if (tagName.equals("activity-alias")) {
//                Activity a = parseActivityAlias(owner, res, parser, attrs, flags, outError);
//                if (a == null) {
//                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
//                    return false;
//                }
//
//                owner.activities.add(a);
//
//            } else if (parser.getName().equals("meta-data")) {
//                // note: application meta-data is stored off to the side, so it can
//                // remain null in the primary copy (we like to avoid extra copies because
//                // it can be large)
//                if ((owner.mAppMetaData = parseMetaData(res, parser, attrs, owner.mAppMetaData,
//                        outError)) == null) {
//                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
//                    return false;
//                }
//
//            } else if (tagName.equals("library")) {
//                sa = res.obtainAttributes(attrs,
//                        R.styleable.AndroidManifestLibrary);
//
//                // Note: don't allow this value to be a reference to a resource
//                // that may change.
//                String lname = sa.getNonResourceString(
//                        R.styleable.AndroidManifestLibrary_name);
//
//                sa.recycle();
//
//                if (lname != null) {
//                    if (owner.libraryNames == null) {
//                        owner.libraryNames = new ArrayList<String>();
//                    }
//                    if (!owner.libraryNames.contains(lname)) {
//                        owner.libraryNames.add(lname.intern());
//                    }
//                }
//
//                XmlUtils.skipCurrentTag(parser);
//
//            } else if (tagName.equals("uses-library")) {
//                sa = res.obtainAttributes(attrs,
//                        R.styleable.AndroidManifestUsesLibrary);
//
//                // Note: don't allow this value to be a reference to a resource
//                // that may change.
//                String lname = sa.getNonResourceString(
//                        R.styleable.AndroidManifestUsesLibrary_name);
//                boolean req = sa.getBoolean(
//                        R.styleable.AndroidManifestUsesLibrary_required,
//                        true);
//
//                sa.recycle();
//
//                if (lname != null) {
//                    if (req) {
//                        if (owner.usesLibraries == null) {
//                            owner.usesLibraries = new ArrayList<String>();
//                        }
//                        if (!owner.usesLibraries.contains(lname)) {
//                            owner.usesLibraries.add(lname.intern());
//                        }
//                    } else {
//                        if (owner.usesOptionalLibraries == null) {
//                            owner.usesOptionalLibraries = new ArrayList<String>();
//                        }
//                        if (!owner.usesOptionalLibraries.contains(lname)) {
//                            owner.usesOptionalLibraries.add(lname.intern());
//                        }
//                    }
//                }
//
//                XmlUtils.skipCurrentTag(parser);
//
//            } else if (tagName.equals("uses-package")) {
//                // Dependencies for app installers; we don't currently try to
//                // enforce this.
//                XmlUtils.skipCurrentTag(parser);
//
//            } else {
//                if (!RIGID_PARSER) {
//                    Slog.w(TAG, "Unknown element under <application>: " + tagName
//                            + " at " + mArchiveSourcePath + " "
//                            + parser.getPositionDescription());
//                    XmlUtils.skipCurrentTag(parser);
//                    continue;
//                } else {
//                    outError[0] = "Bad element under <application>: " + tagName;
//                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
//                    return false;
//                }
//            }
        }

        return true;
    }

}
