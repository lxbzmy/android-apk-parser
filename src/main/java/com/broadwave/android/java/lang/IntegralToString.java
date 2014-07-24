/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.broadwave.android.java.lang;

/**
 * Converts integral types to strings. This class is public but hidden so that it can also be
 * used by java.util.Formatter to speed up %d. This class is in java.lang so that it can take
 * advantage of the package-private String constructor.
 *
 * The most important methods are appendInt/appendLong and intToString(int)/longToString(int).
 * The former are used in the implementation of StringBuilder, StringBuffer, and Formatter, while
 * the latter are used by Integer.toString and Long.toString.
 *
 * The append methods take AbstractStringBuilder rather than Appendable because the latter requires
 * CharSequences, while we only have raw char[]s. Since much of the savings come from not creating
 * any garbage, we can't afford temporary CharSequence instances.
 *
 * One day the performance advantage of the binary/hex/octal specializations will be small enough
 * that we can lose the duplication, but until then this class offers the full set.
 *
 * @hide
 */
public final class IntegralToString {
    /**
     * When appending to an AbstractStringBuilder, this thread-local char[] lets us avoid
     * allocation of a temporary array. (We can't write straight into the AbstractStringBuilder
     * because it's almost as expensive to work out the exact length of the result as it is to
     * do the formatting. We could try being conservative and "delete"-ing the unused space
     * afterwards, but then we'd need to duplicate convertInt and convertLong rather than share
     * the code.)
     */
    private static final ThreadLocal<char[]> BUFFER = new ThreadLocal<char[]>() {
        @Override protected char[] initialValue() {
            return new char[20]; // Maximum length of a base-10 long.
        }
    };

    /**
     * These tables are used to special-case toString computation for
     * small values.  This serves three purposes: it reduces memory usage;
     * it increases performance for small values; and it decreases the
     * number of comparisons required to do the length computation.
     * Elements of this table are lazily initialized on first use.
     * No locking is necessary, i.e., we use the non-volatile, racy
     * single-check idiom.
     */
    private static final String[] SMALL_NONNEGATIVE_VALUES = new String[100];
    private static final String[] SMALL_NEGATIVE_VALUES = new String[100];

    /** TENS[i] contains the tens digit of the number i, 0 <= i <= 99. */
    private static final char[] TENS = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'
    };

    /** Ones [i] contains the tens digit of the number i, 0 <= i <= 99. */
    private static final char[] ONES = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    /**
     * Table for MOD / DIV 10 computation described in Section 10-21
     * of Hank Warren's "Hacker's Delight" online addendum.
     * http://www.hackersdelight.org/divcMore.pdf
     */
    private static final char[] MOD_10_TABLE = {
        0, 1, 2, 2, 3, 3, 4, 5, 5, 6, 7, 7, 8, 8, 9, 0
    };

    /**
     * The digits for every supported radix.
     */
    private static final char[] DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static final char[] UPPER_CASE_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private IntegralToString() {
    }


    /**
     * Returns the string representation of n and leaves sb alone if sb is null.
     * Returns null and appends the string representation of n to sb if sb is non-null.
     */

    /**
     * Inserts the unsigned decimal integer represented by n into the specified
     * character array starting at position cursor.  Returns the index after
     * the last character inserted (i.e., the value to pass in as cursor the
     * next time this method is called). Note that n is interpreted as a large
     * positive integer (not a negative integer) if its sign bit is set.
     */
    private static int intIntoCharArray(char[] buf, int cursor, int n) {
        // Calculate digits two-at-a-time till remaining digits fit in 16 bits
        while ((n & 0xffff0000) != 0) {
            /*
             * Compute q = n/100 and r = n % 100 as per "Hacker's Delight" 10-8.
             * This computation is slightly different from the corresponding
             * computation in intToString: the shifts before and after
             * multiply can't be combined, as that would yield the wrong result
             * if n's sign bit were set.
             */
            int q = (int) ((0x51EB851FL * (n >>> 2)) >>> 35);
            int r = n - 100*q;
            buf[--cursor] = ONES[r];
            buf[--cursor] = TENS[r];
            n = q;
        }

        // Calculate remaining digits one-at-a-time for performance
        while (n != 0) {
            // Compute q = n / 10 and r = n % 10 as per "Hacker's Delight" 10-8
            int q = (0xCCCD * n) >>> 19;
            int r = n - 10*q;
            buf[--cursor] = DIGITS[r];
            n = q;
        }
        return cursor;
    }



    public static StringBuilder appendByteAsHex(StringBuilder sb, byte b, boolean upperCase) {
        char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
        sb.append(digits[(b >> 4) & 0xf]);
        sb.append(digits[b & 0xf]);
        return sb;
    }

    public static String bytesToHexString(byte[] bytes, boolean upperCase) {
        char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
        char[] buf = new char[bytes.length * 2];
        int c = 0;
        for (byte b : bytes) {
            buf[c++] = digits[(b >> 4) & 0xf];
            buf[c++] = digits[b & 0xf];
        }
        return new String(buf);
    }





}