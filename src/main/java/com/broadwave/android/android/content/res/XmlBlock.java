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

package com.broadwave.android.android.content.res;

import com.broadwave.android.android.util.TypedValue;
import com.broadwave.android.com.android.internal.util.XmlUtils;
import com.broadwave.android.util.axml.AXmlResourceParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Wrapper around a compiled XML file.
 *
 * {@hide}
 */
final class XmlBlock {
    private static final boolean DEBUG=false;

    /*public XmlBlock(byte[] data) {
        mAssets = null;
        mStrings = new StringBlock(nativeGetStringBlock(mNative), false);
    }

    public XmlBlock(byte[] data, int offset, int size) {
        mAssets = null;
        mStrings = new StringBlock(nativeGetStringBlock(mNative), false);
    }
*/
  /*  public void close() {
        synchronized (this) {
            if (mOpen) {
                mOpen = false;
                decOpenCountLocked();
            }
        }
    }

    private void decOpenCountLocked() {
        mOpenCount--;
        if (mOpenCount == 0) {
            if (mAssets != null) {
                mAssets.xmlBlockGone(hashCode());
            }
        }
    }*/

    /*public XmlResourceParser newParser() {
        synchronized (this) {
            if (mNative != 0) {
                return new Parser(nativeCreateParseState(mNative), this);
            }
            return null;
        }
    }*/

   /* protected void finalize() throws Throwable {
        close();
    }
*/
    /**
     * Create from an existing xml block native object.  This is
     * -extremely- dangerous -- only use it if you absolutely know what you
     *  are doing!  The given native object must exist for the entire lifetime
     *  of this newly creating XmlBlock.
     */
    XmlBlock(AssetManager assets, int xmlBlock) {
        mAssets = assets;
        mNative = xmlBlock;
//        mStrings = new StringBlock(nativeGetStringBlock(xmlBlock), false);
    }

    private final AssetManager mAssets;
    private final int mNative;
//    /*package*/ final StringBlock mStrings;
    private boolean mOpen = true;
    private int mOpenCount = 1;

    public XmlResourceParser newParser() {
        synchronized (this) {
            if (mNative != 0) {
                return new Parser();
            }
            return null;
        }
    }

    final class Parser extends AXmlResourceParser{

        public int mParseState;

        public String getPooledString(int i) {
            return super.getAttributeValue(i);
        }

    }



}
