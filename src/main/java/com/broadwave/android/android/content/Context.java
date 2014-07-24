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

package com.broadwave.android.android.content;

import com.broadwave.android.android.content.pm.ApplicationInfo;
import com.broadwave.android.android.content.pm.PackageManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface to global information about an application environment.  This is
 * an abstract class whose implementation is provided by
 * the Android system.  It
 * allows access to application-specific resources and classes, as well as
 * up-calls for application-level operations such as launching activities,
 * broadcasting and receiving intents, etc.
 */
public abstract class Context {
    /**
     * File creation mode: the default mode, where the created file can only
     * be accessed by the calling application (or all applications sharing the
     * same user ID).
     * @see #MODE_WORLD_READABLE
     * @see #MODE_WORLD_WRITEABLE
     */
    public static final int MODE_PRIVATE = 0x0000;
    /**
     * File creation mode: allow all other applications to have read access
     * to the created file.
     * @see #MODE_PRIVATE
     * @see #MODE_WORLD_WRITEABLE
     */
    public static final int MODE_WORLD_READABLE = 0x0001;
    /**
     * File creation mode: allow all other applications to have write access
     * to the created file.
     * @see #MODE_PRIVATE
     * @see #MODE_WORLD_READABLE
     */
    public static final int MODE_WORLD_WRITEABLE = 0x0002;
    /**
     * File creation mode: for use with {@link #openFileOutput}, if the file
     * already exists then write data to the end of the existing file
     * instead of erasing it.
     * @see #openFileOutput
     */
    public static final int MODE_APPEND = 0x8000;

    /**
     * SharedPreference loading flag: when set, the file on disk will
     * be checked for modification even if the shared preferences
     * instance is already loaded in this process.  This behavior is
     * sometimes desired in cases where the application has multiple
     * processes, all writing to the same SharedPreferences file.
     * Generally there are better forms of communication between
     * processes, though.
     *
     * <p>This was the legacy (but undocumented) behavior in and
     * before Gingerbread (Android 2.3) and this flag is implied when
     * targetting such releases.  For applications targetting SDK
     * versions <em>greater than</em> Android 2.3, this flag must be
     * explicitly set if desired.
     *
     * @see #getSharedPreferences
     */
    public static final int MODE_MULTI_PROCESS = 0x0004;

    /**
     * Flag for {@link #bindService}: automatically create the service as long
     * as the binding exists.  Note that while this will create the service,
     * its {@link android.app.Service#onStartCommand}
     * method will still only be called due to an
     * explicit call to {@link #startService}.  Even without that, though,
     * this still provides you with access to the service object while the
     * service is created.
     *
     * <p>Note that prior to {@link android.os.Build.VERSION_CODES#ICE_CREAM_SANDWICH},
     * not supplying this flag would also impact how important the system
     * consider's the target service's process to be.  When set, the only way
     * for it to be raised was by binding from a service in which case it will
     * only be important when that activity is in the foreground.  Now to
     * achieve this behavior you must explicitly supply the new flag
     * {@link #BIND_ADJUST_WITH_ACTIVITY}.  For compatibility, old applications
     * that don't specify {@link #BIND_AUTO_CREATE} will automatically have
     * the flags {@link #BIND_WAIVE_PRIORITY} and
     * {@link #BIND_ADJUST_WITH_ACTIVITY} set for them in order to achieve
     * the same result.
     */
    public static final int BIND_AUTO_CREATE = 0x0001;

    /**
     * Flag for {@link #bindService}: include debugging help for mismatched
     * calls to unbind.  When this flag is set, the callstack of the following
     * {@link #unbindService} call is retained, to be printed if a later
     * incorrect unbind call is made.  Note that doing this requires retaining
     * information about the binding that was made for the lifetime of the app,
     * resulting in a leak -- this should only be used for debugging.
     */
    public static final int BIND_DEBUG_UNBIND = 0x0002;

    /**
     * Flag for {@link #bindService}: don't allow this binding to raise
     * the target service's process to the foreground scheduling priority.
     * It will still be raised to at least the same memory priority
     * as the client (so that its process will not be killable in any
     * situation where the client is not killable), but for CPU scheduling
     * purposes it may be left in the background.  This only has an impact
     * in the situation where the binding client is a foreground process
     * and the target service is in a background process.
     */
    public static final int BIND_NOT_FOREGROUND = 0x0004;

    /**
     * Flag for {@link #bindService}: indicates that the client application
     * binding to this service considers the service to be more important than
     * the app itself.  When set, the platform will try to have the out of
     * memory kill the app before it kills the service it is bound to, though
     * this is not guaranteed to be the case.
     */
    public static final int BIND_ABOVE_CLIENT = 0x0008;

    /**
     * Flag for {@link #bindService}: allow the process hosting the bound
     * service to go through its normal memory management.  It will be
     * treated more like a running service, allowing the system to
     * (temporarily) expunge the process if low on memory or for some other
     * whim it may have, and being more aggressive about making it a candidate
     * to be killed (and restarted) if running for a long time.
     */
    public static final int BIND_ALLOW_OOM_MANAGEMENT = 0x0010;

    /**
     * Flag for {@link #bindService}: don't impact the scheduling or
     * memory management priority of the target service's hosting process.
     * Allows the service's process to be managed on the background LRU list
     * just like a regular application process in the background.
     */
    public static final int BIND_WAIVE_PRIORITY = 0x0020;

    /**
     * Flag for {@link #bindService}: this service is very important to
     * the client, so should be brought to the foreground process level
     * when the client is.  Normally a process can only be raised to the
     * visibility level by a client, even if that client is in the foreground.
     */
    public static final int BIND_IMPORTANT = 0x0040;

    /**
     * Flag for {@link #bindService}: If binding from an activity, allow the
     * target service's process importance to be raised based on whether the
     * activity is visible to the user, regardless whether another flag is
     * used to reduce the amount that the client process's overall importance
     * is used to impact it.
     */
    public static final int BIND_ADJUST_WITH_ACTIVITY = 0x0080;

    /**
     * Flag for {@link #bindService}: Don't consider the bound service to be
     * visible, even if the caller is visible.
     * @hide
     */
    public static final int BIND_NOT_VISIBLE = 0x40000000;

    /** Return PackageManager instance to find global package information. */
    public abstract PackageManager getPackageManager();

    /**
     * Return the context of the single, global Application object of the
     * current process.  This generally should only be used if you need a
     * Context whose lifecycle is separate from the current context, that is
     * tied to the lifetime of the process rather than the current component.
     *
     * <p>Consider for example how this interacts with
     * {@link #registerReceiver(BroadcastReceiver, IntentFilter)}:
     * <ul>
     * <li> <p>If used from an Activity context, the receiver is being registered
     * within that activity.  This means that you are expected to unregister
     * before the activity is done being destroyed; in fact if you do not do
     * so, the framework will clean up your leaked registration as it removes
     * the activity and log an error.  Thus, if you use the Activity context
     * to register a receiver that is static (global to the process, not
     * associated with an Activity instance) then that registration will be
     * removed on you at whatever point the activity you used is destroyed.
     * <li> <p>If used from the Context returned here, the receiver is being
     * registered with the global state associated with your application.  Thus
     * it will never be unregistered for you.  This is necessary if the receiver
     * is associated with static data, not a particular component.  However
     * using the ApplicationContext elsewhere can easily lead to serious leaks
     * if you forget to unregister, unbind, etc.
     * </ul>
     */
    public abstract Context getApplicationContext();

    /** Return the name of this application's package. */
    public abstract String getPackageName();

    /** Return the full application info for this context's package. */
    public abstract ApplicationInfo getApplicationInfo();

    /**
     * Return the full path to this context's primary Android package.
     * The Android package is a ZIP file which contains application's
     * primary code and assets.
     *
     * <p>Note: this is not generally useful for applications, since they should
     * not be directly accessing the file system.
     *
     * @return String Path to the code and assets.
     */
    public abstract String getPackageCodePath();

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.os.PowerManager} for controlling power management,
     * including "wake locks," which let you keep the device on while
     * you're running long tasks.
     */
    public static final String POWER_SERVICE = "power";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.view.WindowManager} for accessing the system's window
     * manager.
     *
     * @see #getSystemService
     * @see android.view.WindowManager
     */
    public static final String WINDOW_SERVICE = "window";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.view.LayoutInflater} for inflating layout resources in this
     * context.
     *
     * @see #getSystemService
     * @see android.view.LayoutInflater
     */
    public static final String LAYOUT_INFLATER_SERVICE = "layout_inflater";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.accounts.AccountManager} for receiving intents at a
     * time of your choosing.
     *
     * @see #getSystemService
     * @see android.accounts.AccountManager
     */
    public static final String ACCOUNT_SERVICE = "account";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.app.ActivityManager} for interacting with the global
     * system state.
     *
     * @see #getSystemService
     * @see android.app.ActivityManager
     */
    public static final String ACTIVITY_SERVICE = "activity";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.app.AlarmManager} for receiving intents at a
     * time of your choosing.
     *
     * @see #getSystemService
     * @see android.app.AlarmManager
     */
    public static final String ALARM_SERVICE = "alarm";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.app.NotificationManager} for informing the user of
     * background events.
     *
     * @see #getSystemService
     * @see android.app.NotificationManager
     */
    public static final String NOTIFICATION_SERVICE = "notification";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.view.accessibility.AccessibilityManager} for giving the user
     * feedback for UI events through the registered event listeners.
     *
     * @see #getSystemService
     * @see android.view.accessibility.AccessibilityManager
     */
    public static final String ACCESSIBILITY_SERVICE = "accessibility";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.app.NotificationManager} for controlling keyguard.
     *
     * @see #getSystemService
     * @see android.app.KeyguardManager
     */
    public static final String KEYGUARD_SERVICE = "keyguard";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.location.LocationManager} for controlling location
     * updates.
     *
     * @see #getSystemService
     * @see android.location.LocationManager
     */
    public static final String LOCATION_SERVICE = "location";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.location.CountryDetector} for detecting the country that
     * the user is in.
     *
     * @hide
     */
    public static final String COUNTRY_DETECTOR = "country_detector";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.app.SearchManager} for handling searches.
     *
     * @see #getSystemService
     * @see android.app.SearchManager
     */
    public static final String SEARCH_SERVICE = "search";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.hardware.SensorManager} for accessing sensors.
     *
     * @see #getSystemService
     * @see android.hardware.SensorManager
     */
    public static final String SENSOR_SERVICE = "sensor";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.os.storage.StorageManager} for accessing system storage
     * functions.
     *
     * @see #getSystemService
     * @see android.os.storage.StorageManager
     */
    public static final String STORAGE_SERVICE = "storage";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * com.android.server.WallpaperService for accessing wallpapers.
     *
     * @see #getSystemService
     */
    public static final String WALLPAPER_SERVICE = "wallpaper";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.os.Vibrator} for interacting with the vibration hardware.
     *
     * @see #getSystemService
     * @see android.os.Vibrator
     */
    public static final String VIBRATOR_SERVICE = "vibrator";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.app.StatusBarManager} for interacting with the status bar.
     *
     * @see #getSystemService
     * @see android.app.StatusBarManager
     * @hide
     */
    public static final String STATUS_BAR_SERVICE = "statusbar";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.net.ConnectivityManager} for handling management of
     * network connections.
     *
     * @see #getSystemService
     * @see android.net.ConnectivityManager
     */
    public static final String CONNECTIVITY_SERVICE = "connectivity";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.net.ThrottleManager} for handling management of
     * throttling.
     *
     * @hide
     * @see #getSystemService
     * @see android.net.ThrottleManager
     */
    public static final String THROTTLE_SERVICE = "throttle";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.net.NetworkManagementService} for handling management of
     * system network services
     *
     * @hide
     * @see #getSystemService
     * @see android.net.NetworkManagementService
     */
    public static final String NETWORKMANAGEMENT_SERVICE = "network_management";

    /** {@hide} */
    public static final String NETWORK_STATS_SERVICE = "netstats";
    /** {@hide} */
    public static final String NETWORK_POLICY_SERVICE = "netpolicy";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.net.wifi.WifiManager} for handling management of
     * Wi-Fi access.
     *
     * @see #getSystemService
     * @see android.net.wifi.WifiManager
     */
    public static final String WIFI_SERVICE = "wifi";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.net.wifi.p2p.WifiP2pManager} for handling management of
     * Wi-Fi peer-to-peer connections.
     *
     * @see #getSystemService
     * @see android.net.wifi.p2p.WifiP2pManager
     */
    public static final String WIFI_P2P_SERVICE = "wifip2p";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.media.AudioManager} for handling management of volume,
     * ringer modes and audio routing.
     *
     * @see #getSystemService
     * @see android.media.AudioManager
     */
    public static final String AUDIO_SERVICE = "audio";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.telephony.TelephonyManager} for handling management the
     * telephony features of the device.
     *
     * @see #getSystemService
     * @see android.telephony.TelephonyManager
     */
    public static final String TELEPHONY_SERVICE = "phone";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.text.ClipboardManager} for accessing and modifying
     * the contents of the global clipboard.
     *
     * @see #getSystemService
     * @see android.text.ClipboardManager
     */
    public static final String CLIPBOARD_SERVICE = "clipboard";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.view.inputmethod.InputMethodManager} for accessing input
     * methods.
     *
     * @see #getSystemService
     */
    public static final String INPUT_METHOD_SERVICE = "input_method";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.view.textservice.TextServicesManager} for accessing
     * text services.
     *
     * @see #getSystemService
     */
    public static final String TEXT_SERVICES_MANAGER_SERVICE = "textservices";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.appwidget.AppWidgetManager} for accessing AppWidgets.
     *
     * @hide
     * @see #getSystemService
     */
    public static final String APPWIDGET_SERVICE = "appwidget";

    /**
     * Use with {@link #getSystemService} to retrieve an
     * {@link android.app.backup.IBackupManager IBackupManager} for communicating
     * with the backup mechanism.
     * @hide
     *
     * @see #getSystemService
     */
    public static final String BACKUP_SERVICE = "backup";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.os.DropBoxManager} instance for recording
     * diagnostic logs.
     * @see #getSystemService
     */
    public static final String DROPBOX_SERVICE = "dropbox";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.app.admin.DevicePolicyManager} for working with global
     * device policy management.
     *
     * @see #getSystemService
     */
    public static final String DEVICE_POLICY_SERVICE = "device_policy";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.app.UiModeManager} for controlling UI modes.
     *
     * @see #getSystemService
     */
    public static final String UI_MODE_SERVICE = "uimode";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.app.DownloadManager} for requesting HTTP downloads.
     *
     * @see #getSystemService
     */
    public static final String DOWNLOAD_SERVICE = "download";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.nfc.NfcManager} for using NFC.
     *
     * @see #getSystemService
     */
    public static final String NFC_SERVICE = "nfc";

    /**
     * Use with {@link #getSystemService} to retrieve a
     * {@link android.net.sip.SipManager} for accessing the SIP related service.
     *
     * @see #getSystemService
     */
    /** @hide */
    public static final String SIP_SERVICE = "sip";

    /**
     * Use with {@link #getSystemService} to retrieve a {@link
     * android.hardware.usb.UsbManager} for access to USB devices (as a USB host)
     * and for controlling this device's behavior as a USB device.
     *
     * @see #getSystemService
     * @see android.harware.usb.UsbManager
     */
    public static final String USB_SERVICE = "usb";

    /**
     * Flag for use with {@link #createPackageContext}: include the application
     * code with the context.  This means loading code into the caller's
     * process, so that {@link #getClassLoader()} can be used to instantiate
     * the application's classes.  Setting this flags imposes security
     * restrictions on what application context you can access; if the
     * requested application can not be safely loaded into your process,
     * java.lang.SecurityException will be thrown.  If this flag is not set,
     * there will be no restrictions on the packages that can be loaded,
     * but {@link #getClassLoader} will always return the default system
     * class loader.
     */
    public static final int CONTEXT_INCLUDE_CODE = 0x00000001;

    /**
     * Flag for use with {@link #createPackageContext}: ignore any security
     * restrictions on the Context being requested, allowing it to always
     * be loaded.  For use with {@link #CONTEXT_INCLUDE_CODE} to allow code
     * to be loaded into a process even when it isn't safe to do so.  Use
     * with extreme care!
     */
    public static final int CONTEXT_IGNORE_SECURITY = 0x00000002;

    /**
     * Flag for use with {@link #createPackageContext}: a restricted context may
     * disable specific features. For instance, a View associated with a restricted
     * context would ignore particular XML attributes.
     */
    public static final int CONTEXT_RESTRICTED = 0x00000004;

    /**
     * Return a new Context object for the given application name.  This
     * Context is the same as what the named application gets when it is
     * launched, containing the same resources and class loader.  Each call to
     * this method returns a new instance of a Context object; Context objects
     * are not shared, however they share common state (Resources, ClassLoader,
     * etc) so the Context instance itself is fairly lightweight.
     *
     * <p>Throws {@link PackageManager.NameNotFoundException} if there is no
     * application with the given package name.
     *
     * <p>Throws {@link java.lang.SecurityException} if the Context requested
     * can not be loaded into the caller's process for security reasons (see
     * {@link #CONTEXT_INCLUDE_CODE} for more information}.
     *
     * @param packageName Name of the application's package.
     * @param flags Option flags, one of {@link #CONTEXT_INCLUDE_CODE}
     *              or {@link #CONTEXT_IGNORE_SECURITY}.
     *
     * @return A Context for the application.
     *
     * @throws java.lang.SecurityException
     * @throws PackageManager.NameNotFoundException if there is no application with
     * the given package name
     */
    public abstract Context createPackageContext(String packageName,
            int flags) throws PackageManager.NameNotFoundException;
}
