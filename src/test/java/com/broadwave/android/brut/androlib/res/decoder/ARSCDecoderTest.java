package com.broadwave.android.brut.androlib.res.decoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

import com.broadwave.android.brut.androlib.AndrolibException;
import com.broadwave.android.brut.androlib.res.data.ResID;
import com.broadwave.android.brut.androlib.res.data.ResPackage;
import com.broadwave.android.brut.androlib.res.data.ResResSpec;
import com.broadwave.android.brut.androlib.res.data.ResTable;
import com.broadwave.android.brut.androlib.res.decoder.ARSCDecoder;
import com.broadwave.android.brut.androlib.res.decoder.ARSCDecoder.ARSCData;

public class ARSCDecoderTest {

    @Test
    public void test() throws FileNotFoundException, AndrolibException {

        InputStream arscStream = new FileInputStream(new File(
                "E:/workspace4/axml/src/test/resources/resources.arsc"));
        ARSCData decode = ARSCDecoder.decode(arscStream, true, true);

        ResTable resTable = decode.getResTable();
        ResPackage resPackage = decode.getOnePackage();
        ResTable resTable2 = resPackage.getResTable();
        ResResSpec resSpec = resPackage.getResSpec(new ResID(16842763));
        ResPackage onePackage = decode.getOnePackage();
        System.out.println(resSpec);
        System.out.println(resSpec.getName());
        System.out.println(resSpec.getFullName());
        System.out.println("finish");

        Map<String, String> packageInfo = resTable.getPackageInfo();
        System.out.println(packageInfo);
        resTable.addPackage(resPackage, true);
        resTable.getResSpec(16842763);
    }

}
