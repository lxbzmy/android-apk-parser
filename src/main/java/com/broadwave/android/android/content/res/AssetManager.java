package com.broadwave.android.android.content.res;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.broadwave.android.android.content.res.XmlBlock.Parser;
import com.broadwave.android.android.content.res.XmlResourceParser;
import com.broadwave.android.android.util.TypedValue;
import com.broadwave.android.brut.androlib.AndrolibException;
import com.broadwave.android.brut.androlib.res.data.ResConfig;
import com.broadwave.android.brut.androlib.res.data.ResConfigFlags;
import com.broadwave.android.brut.androlib.res.data.ResID;
import com.broadwave.android.brut.androlib.res.data.ResPackage;
import com.broadwave.android.brut.androlib.res.data.ResResSpec;
import com.broadwave.android.brut.androlib.res.data.ResResource;
import com.broadwave.android.brut.androlib.res.data.ResTable;
import com.broadwave.android.brut.androlib.res.data.ResType;
import com.broadwave.android.brut.androlib.res.data.value.ResFileValue;
import com.broadwave.android.brut.androlib.res.data.value.ResScalarValue;
import com.broadwave.android.brut.androlib.res.data.value.ResStringValue;
import com.broadwave.android.brut.androlib.res.data.value.ResValue;
import com.broadwave.android.brut.androlib.res.decoder.ARSCDecoder;
import com.broadwave.android.brut.androlib.res.decoder.ARSCDecoder.ARSCData;
import com.broadwave.android.util.axml.AXmlResourceParser;
import com.google.common.io.ByteStreams;

/**
 * Mock AssetManager.
 * <p>
 * Load *.APK and process AndroidManifest.xml,resources.arsc
 *
 * @author lxb
 *
 */
/**
 * @author lxb
 *
 */
public class AssetManager {

    /* modes used when opening an asset */

    private static final String ANDROID_RESOURCE_LOCATION = "/com/broadwave/android/android/resources.arsc";
    /**
     * Mode for {@link #open(String, int)}: no specific information about how
     * data will be accessed.
     */
    public static final int ACCESS_UNKNOWN = 0;
    /**
     * Mode for {@link #open(String, int)}: Read chunks, and seek forward and
     * backward.
     */
    public static final int ACCESS_RANDOM = 1;
    /**
     * Mode for {@link #open(String, int)}: Read sequentially, with an
     * occasional forward seek.
     */
    public static final int ACCESS_STREAMING = 2;
    /**
     * Mode for {@link #open(String, int)}: Attempt to load contents into
     * memory, for fast small reads.
     */
    public static final int ACCESS_BUFFER = 3;

    private static final String TAG = "AssetManager";
    private static final boolean localLOGV = false || false;

    private static final boolean DEBUG_REFS = false;

    private static final Object sSync = new Object();

    /* package */static final int STYLE_NUM_ENTRIES = 6;
    /* package */static final int STYLE_TYPE = 0;
    /* package */static final int STYLE_DATA = 1;
    /* package */static final int STYLE_ASSET_COOKIE = 2;
    /* package */static final int STYLE_RESOURCE_ID = 3;
    /* package */static final int STYLE_CHANGING_CONFIGURATIONS = 4;
    /* package */static final int STYLE_DENSITY = 5;

    private ZipFile file;

    private ResConfigFlags flags;

    public AssetManager() {
        super();
        // mcc = 0;
        // mnc = 0;
        // language = new char[] { '\00', '\00' };
        // country = new char[] { '\00', '\00' };
        // layoutDirection = SCREENLAYOUT_LAYOUTDIR_ANY;
        // orientation = ORIENTATION_ANY;
        // touchscreen = TOUCHSCREEN_ANY;
        // density = DENSITY_DEFAULT;
        // keyboard = KEYBOARD_ANY;
        // navigation = NAVIGATION_ANY;
        // inputFlags = KEYSHIDDEN_ANY | NAVHIDDEN_ANY;
        // screenWidth = 0;
        // screenHeight = 0;
        // sdkVersion = 0;
        // screenLayout = SCREENLONG_ANY | SCREENSIZE_ANY;
        // uiMode = UI_MODE_TYPE_ANY | UI_MODE_NIGHT_ANY;
        // smallestScreenWidthDp = 0;
        // screenWidthDp = 0;
        // screenHeightDp = 0;
        // isInvalid = false;
        // mQualifiers = "";
        char[] lang = { 'z', 'h' };
        char[] country = { 'c', 'n' };
        ResConfigFlags flag = new ResConfigFlags(
                (short) 0,
                (short) 0,
                lang,
                country,
                ResConfigFlags.SCREENLAYOUT_LAYOUTDIR_ANY,
                ResConfigFlags.ORIENTATION_ANY,
                ResConfigFlags.TOUCHSCREEN_ANY,
                ResConfigFlags.DENSITY_XXHIGH,
                ResConfigFlags.KEYBOARD_ANY,
                ResConfigFlags.NAVIGATION_ANY,
                ResConfigFlags.KEYSHIDDEN_ANY,
                (short) 2048,
                (short) 1536,
                (short) 19,
                (byte) (ResConfigFlags.SCREENLONG_ANY | ResConfigFlags.SCREENSIZE_XLARGE),
                (byte) ((byte) ResConfigFlags.UI_MODE_TYPE_ANY | ResConfigFlags.UI_MODE_NIGHT_ANY),
                (short) 0, (short) 0, (short) 0, false);
        this.flags = flag;
        // public ResConfigFlags(short s, short t, char[] lang, char[]
        // country2,
        // short screenlayoutLayoutdirLtr, byte orientationLand,
        // byte touchscreenNotouch, short densityXxhigh, byte
        // keyboardQwerty,
        // byte navhiddenYes, byte keyshiddenAny, int i, int j, int k, int
        // l,
        // int m, short u, short v, short w, boolean b) {
        // // TODO Auto-generated constructor stub
        // }

        // short mcc, short mnc, char[] language,
        // char[] country, short layoutDirection, byte orientation,
        // byte touchscreen, short density, byte keyboard, byte navigation,
        // byte inputFlags, short screenWidth, short screenHeight,
        // short sdkVersion, byte screenLayout, byte uiMode,
        // short smallestScreenWidthDp, short screenWidthDp,
        // short screenHeightDp, boolean isInvalid
    }

    /**
     * do nothing.
     */
    public final void setConfiguration(int mcc, int mnc, String locale,
            int orientation, int touchscreen, int density, int keyboard,
            int keyboardHidden, int navigation, int screenWidth,
            int screenHeight, int smallestScreenWidthDp, int screenWidthDp,
            int screenHeightDp, int screenLayout, int uiMode, int majorVersion) {
        // TODO copy config from outer into asset manager.
    }

    public final boolean isUpToDate() {
        return true;
    };

    Locale locale = Locale.CHINESE;

    /**
     * Change the locale being used by this asset manager. Not for use by
     * applications.
     */
    public final void setLocale(String locale) {
        this.locale = new Locale(locale);
    };

    private String apk = null;

    /**
     * Add an additional set of assets to the asset manager. This can be either
     * a directory or ZIP file. Not for use by applications. Returns the cookie
     * of the added asset, or 0 on failure. {@hide}
     */
    public final int addAssetPath(String path) {
        if (apk != null) {
            return 0;
        } else {
            apk = path;
            try {
                this.file = new ZipFile(new File(path), ZipFile.OPEN_READ);
                mOpen = true;
                return 1;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    /**
     * 在APK压缩文件中找到AndroidManifest.xml文件流
     *
     * @param filename
     * @throws IOException
     * @return 文件的字节流，使用死循环读取出来。
     */
    public byte[] getXmlBlock(String filename) throws IOException {
        ZipEntry entry = file.getEntry(filename);
        InputStream inputStream = file.getInputStream(entry);
        byte[] byteArray = ByteStreams.toByteArray(inputStream);
        return byteArray;
    }

    AXmlResourceParser manifest = null;

    /**
     * @param filename
     * @return 解析器
     * @throws IOException
     */
    public XmlResourceParser openXmlResourceParser(String filename)
            throws IOException {
        ZipEntry entry = file.getEntry(filename);
        InputStream inputStream = file.getInputStream(entry);
        AXmlResourceParser parser = (AXmlResourceParser) new XmlBlock(null, 1)
                .newParser();
        parser.open(inputStream);
        manifest = parser;
        return parser;
    }

    /**
     * 关闭ZIP文件引用。
     *
     * @throws IOException
     */
    public void close() {
        mOpen = false;
        try {
            this.file.close();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Retrieve the string value associated with a particular resource
     * identifier for the current configuration / skin.
     */
    final CharSequence getResourceText(int ident) {
        // use locale to get string value;
        try {
            ResResSpec resSpec = resTable.getResSpec(ident);
            boolean found = false;
            // lang=zh
            // r=CN
            // TODO 从外部传入本地参数。
            char[] lang = { 'z', 'h' };
            char[] reg = { 'C', 'N' };
            Set<ResResource> set = resSpec.listResources();
            for (ResResource item : set) {
                if (Arrays.equals(lang, item.getConfig().getFlags().language)) {
                    found = true;
                    ResValue value = item.getValue();
                    if (value instanceof ResStringValue) {
                        // directly treat string resource as string
                        // typedvalue.
                        return ((ResScalarValue) value).encodeAsResXmlValue();
                    } else {
                        return value.toString();
                    }
                }
            }
            return resSpec.getDefaultResource().getValue().toString();

        } catch (AndrolibException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns true if the resource was found, filling in mRetStringBlock and
     * mRetData.
     */
    private final int loadResourceValue(int ident, short density,
            TypedValue outValue, boolean resolve) {
        try {
            ResResSpec resSpec = resTable.getResSpec(ident);
            // get last resource
            Set<ResResource> set = resSpec.listResources();
            ResResource res = null;
            for (ResResource item : set) {
                res = item;
            }
            // ResResource res = resSpec.getResource(this.flags);
            // //TODO 判断-xxhdpi -xhdpi -hdpi -mdpi -ldpi
            // Collections.sort(set, new Comparator<String>() {
            // public int compare(String o1, String o2) {
            // Integer i1 = Integer.parseInt(o1);
            // Integer i2 = Integer.parseInt(o2);
            // return return (i1 > i2 ? -1 : (i1 == i2 ? 0 : 1));
            // }
            // });

            outValue.type = resSpec.getId().type;
            outValue.data = res.getResSpec().getId().id;
            outValue.density = res.getConfig().getFlags().density;
            outValue.resourceId = res.getResSpec().getId().id;
            ResValue value = res.getValue();
            if (value instanceof ResFileValue) {
                outValue.string = ((ResFileValue) value).getPath();// 是根据容器选择好具体png图片的路径。
            } else {
                outValue.string = res.getFilePath();// 是根据容器选择好具体png图片的路径。
            }

        } catch (AndrolibException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    final boolean getResourceValue(int ident, int density, TypedValue outValue,
            boolean resolveRefs) {
        int block = loadResourceValue(ident, (short) density, outValue,
                resolveRefs);
        if (block >= 0) {
            if (outValue.type != TypedValue.TYPE_STRING) {
                return true;
            }
            // TODO
            // outValue.string = mStringBlocks[block].get(outValue.data);
            return true;
        }
        return false;
    }

    ResPackage androidResource;
    ResTable resTable = null;
    private boolean mOpen = true;

    /**
     * Load framework resource table once.
     *
     * @throws AndrolibException
     */
    private void loadFrameworkResourceTable() throws AndrolibException {
        if (androidResource == null) {
            InputStream inputStream = getClass().getResourceAsStream(
                    ANDROID_RESOURCE_LOCATION);
            ARSCData decode = ARSCDecoder.decode(inputStream, true, true);
            androidResource = decode.getOnePackage();
        }
    }

    private void loadResourceTable() throws IOException, AndrolibException {
        loadFrameworkResourceTable();
        if (resTable == null) {
            ZipEntry entry = file.getEntry("resources.arsc");
            if (entry != null) {
                InputStream inputStream = file.getInputStream(entry);
                ARSCData decode = ARSCDecoder.decode(inputStream, true, true);
                ResPackage[] packages = decode.getPackages();
                resTable = decode.getResTable();
                for (ResPackage item : packages) {
                    if (item.getId() != 1) {
                        //skip and jump android internal resource(id=1 and name=android);
                        resTable.addPackage(item, true);
                    }
                }
                // 很奇怪吧，有些老板的APK里面还有android resource。
                // if(!resTable.hasPackage(androidResource.getId())){
                resTable.addPackage(androidResource, false);
                // }
            } else {
                // null
            }
        }
    }

    /**
     * {@hide} Open a non-asset in a specified package. Not for use by
     * applications.
     *
     * @param cookie
     *            Identifier of the package to be opened.
     * @param fileName
     *            Name of the asset to retrieve.
     * @param accessMode
     *            Desired access mode for retrieving the data.
     */
    public final InputStream openNonAsset(int cookie, String fileName,
            int accessMode) throws IOException {
        synchronized (this) {
            if (!mOpen) {
                throw new RuntimeException("Assetmanager has been closed");
            }
            ZipEntry entry = this.file.getEntry(fileName);
            if (entry != null) {
                return this.file.getInputStream(entry);
            }
        }
        throw new FileNotFoundException("Asset absolute file: " + fileName);
    }

    /**
     * 填充TypedArray
     *
     * @param mParseState
     * @param attrs
     *            编译后的字符串资源id列表。
     * @param array
     *            和attrs长度一样的TypedArray
     */
    public void retrieveAttributes(int mParseState, int[] attrs,
            TypedArray array) {
        try {
            loadResourceTable();
        } catch (AndrolibException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TypedValue nullValue = new TypedValue();
        nullValue.type = TypedValue.TYPE_NULL;
        for (int i = 0; i < attrs.length; i++) {
            try {
                ResResSpec resSpec = resTable.getResSpec(attrs[i]);
                String seek = resSpec.getName();
                boolean found = false;
                for (int j = 0; j < manifest.getAttributeCount(); j++) {
                    String name = manifest.getAttributeName(j);
                    if (seek.equals(name)) {
                        TypedValue item = new TypedValue();
                        item.type = manifest.getAttributeValueType(j);
                        item.data = manifest.getAttributeValueData(j);
                        item.string = manifest.getAttributeValue(j);
                        item.changingConfigurations = 0;
                        if (item.type == TypedValue.TYPE_REFERENCE) {
                            item.resourceId = item.data;
                            ResResSpec res = resTable.getResSpec(item.data);
                            // "drawable""string";
                            if ("string".equals(res.getType().getName())) {
                                ResValue value = res.getDefaultResource()
                                        .getValue();
                                if (value instanceof ResStringValue) {
                                    // directly treat string resource as string
                                    // typedvalue.
                                    item.type = TypedValue.TYPE_STRING;
                                    item.string = ((ResScalarValue) value)
                                            .encodeAsResXmlValue();
                                } else {
                                    // throw new
                                    // RuntimeException("not impled.");
                                }
                            }
                        }
                        // manifest.getAR
                        found = true;
                        array.addValueAt(item, i);
                        break;
                    }
                }
                if (!found) {
                    // mock null
                    array.addValueAt(nullValue, i);
                }

            } catch (AndrolibException e) {
                e.printStackTrace();
            }
        }
        // outValue.type = type;
        // outValue.data = data[index+AssetManager.STYLE_DATA];1
        // outValue.assetCookie = data[index+AssetManager.STYLE_ASSET_COOKIE];2
        // outValue.resourceId = data[index+AssetManager.STYLE_RESOURCE_ID];3
        // outValue.changingConfigurations =
        // data[index+AssetManager.STYLE_CHANGING_CONFIGURATIONS];4
        // outValue.density = data[index+AssetManager.STYLE_DENSITY];5
        // outValue.string = (type == TypedValue.TYPE_STRING) ?
        // loadStringValueAt(index) : null;

    }

    private String fillTypedValue(TypedValue value, AXmlResourceParser parser,
            int index) {
        int type = parser.getAttributeValueType(index);
        int data = parser.getAttributeValueData(index);
        if (type == TypedValue.TYPE_STRING) {
            return parser.getAttributeValue(index);
        }
        if (type == TypedValue.TYPE_ATTRIBUTE) {
            return String.format("?%s%08X", getPackage(data), data);
        }
        if (type == TypedValue.TYPE_REFERENCE) {
            return String.format("@%s%08X", getPackage(data), data);
        }
        if (type == TypedValue.TYPE_FLOAT) {
            return String.valueOf(Float.intBitsToFloat(data));
        }
        if (type == TypedValue.TYPE_INT_HEX) {
            return String.format("0x%08X", data);
        }
        if (type == TypedValue.TYPE_INT_BOOLEAN) {
            return data != 0 ? "true" : "false";
        }
        if (type == TypedValue.TYPE_DIMENSION) {
            return Float.toString(complexToFloat(data))
                    + DIMENSION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
        }
        if (type == TypedValue.TYPE_FRACTION) {
            return Float.toString(complexToFloat(data))
                    + FRACTION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
        }
        if (type >= TypedValue.TYPE_FIRST_COLOR_INT
                && type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return String.format("#%08X", data);
        }
        if (type >= TypedValue.TYPE_FIRST_INT
                && type <= TypedValue.TYPE_LAST_INT) {
            return String.valueOf(data);
        }
        return String.format("<0x%X, type 0x%02X>", data, type);
    }

    private static String getPackage(int id) {
        if (id >>> 24 == 1) {
            return "android:";
        }
        return "";
    }

    public static float complexToFloat(int complex) {
        return (float) (complex & 0xFFFFFF00) * RADIX_MULTS[(complex >> 4) & 3];
    }

    private static final float RADIX_MULTS[] = { 0.00390625F, 3.051758E-005F,
            1.192093E-007F, 4.656613E-010F };
    private static final String DIMENSION_UNITS[] = { "px", "dip", "sp", "pt",
            "in", "mm", "", "" };
    private static final String FRACTION_UNITS[] = { "%", "%p", "", "", "", "",
            "", "" };
}
