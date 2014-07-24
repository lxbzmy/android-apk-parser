Android APK Parser.
========================

Use Android Interface and other tools to parse APK file get manifest.
使用android api 做原型实现的APK包解析,用于J2SE环境中读取APK做验证。

Feature:
-------------

- decode manifest.xml
- decode resources.
- build PackageInfo.
- get ic_lanucher.png as BuffererdImage.

Dependence
-------------
- copy some Android 4.2.2 API,平台的API基于4.2.2（KikKat,API19）.
- eg. com.android.interal.R android.R
- set screen resolution as max.屏幕分辨率假定为最高值。
- pass Locale in constructor.在构造的时候设置好。


TODO
-------------
- AndroidManifest.xml
- ICON
- locallization app name
- Signature

example
--------------


    import com.broadwave.android.util.PackageParserUtil.PackageParserUtil()
    import com.broadwave.android.content.pm.PackageInfo;
    ...
    //default locale is zh_CN
    File f = new File(bash.apk);
    //create packageParserUtil;
    PackageParserUtil util = new PackageParserUtil();
    PackageInfo info = util.parsePackageInfo(f);
    //APK info.
    assertThat(info.packageName, equalTo("jackpal.androidterm"));
    assertThat(info.versionCode, equalTo(53));
    assertThat(info.versionName, equalTo("1.0.52"));
    assertThat(info.applicationInfo.localizedLabel.toString(),
            equalTo("终端模拟器"));
    assertThat(info.applicationInfo.iconImage.getWidth(null), equalTo(96));//icon

License
------------
    /**
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