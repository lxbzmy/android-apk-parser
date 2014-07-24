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

package com.broadwave.android.android.util;


import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @hide
 */
public final class Slog {

    private Slog() {
    }

    public static int v(String tag, String msg) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.VERBOSE, tag, msg);
    	System.out.println(msg);return 1;
    }

    public static int v(String tag, String msg, Throwable tr) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.VERBOSE, tag,
//                msg + '\n' + Log.getStackTraceString(tr));
                System.out.println(msg);return 1;
    }

    public static int d(String tag, String msg) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.DEBUG, tag, msg);
    	System.out.println(msg);return 1;
    }

    public static int d(String tag, String msg, Throwable tr) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.DEBUG, tag,
//                msg + '\n' + Log.getStackTraceString(tr));
    	System.out.println(msg);return 1;
    }

    public static int i(String tag, String msg) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.INFO, tag, msg);
    	System.out.println(msg);return 1;
    }

    public static int i(String tag, String msg, Throwable tr) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.INFO, tag,
//                msg + '\n' + Log.getStackTraceString(tr));
    	System.out.println(msg);return 1;
    }

    public static int w(String tag, String msg) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.WARN, tag, msg);
    	System.out.println(msg);return 1;
    }

    public static int w(String tag, String msg, Throwable tr) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.WARN, tag,
//                msg + '\n' + Log.getStackTraceString(tr));
    	System.out.println(msg+Log.getStackTraceString(tr));return 1;
    }

    public static int w(String tag, Throwable tr) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.WARN, tag, Log.getStackTraceString(tr));
    	System.out.println(Log.getStackTraceString(tr));return 1;
    }

    public static int e(String tag, String msg) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.ERROR, tag, msg);
    	System.out.println(msg);return 1;
    }

    public static int e(String tag, String msg, Throwable tr) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, Log.ERROR, tag,
//                msg + '\n' + Log.getStackTraceString(tr));
    	System.out.println(msg);return 1;
    }

    public static int println(int priority, String tag, String msg) {
        //return Log.println_native(Log.LOG_ID_SYSTEM, priority, tag, msg);
    	System.out.println(msg);return 1;
    }
}

