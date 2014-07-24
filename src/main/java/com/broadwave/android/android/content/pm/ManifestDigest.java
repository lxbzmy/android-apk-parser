package com.broadwave.android.android.content.pm;

import com.broadwave.android.android.os.Parcelable;
import com.broadwave.android.android.util.Base64;

import java.util.Arrays;
import java.util.jar.Attributes;

/**
 * Represents the manifest digest for a package. This is suitable for comparison
 * of two packages to know whether the manifests are identical.
 *
 * @hide
 */
public class ManifestDigest implements Parcelable {
    /** The digest of the manifest in our preferred order. */
    private final byte[] mDigest;

    /** Digest field names to look for in preferred order. */
    private static final String[] DIGEST_TYPES = {
            "SHA1-Digest", "SHA-Digest", "MD5-Digest",
    };

    /** What we print out first when toString() is called. */
    private static final String TO_STRING_PREFIX = "ManifestDigest {mDigest=";

    ManifestDigest(byte[] digest) {
        mDigest = digest;
    }

    static ManifestDigest fromAttributes(Attributes attributes) {
        if (attributes == null) {
            return null;
        }

        String encodedDigest = null;

        for (int i = 0; i < DIGEST_TYPES.length; i++) {
            final String value = attributes.getValue(DIGEST_TYPES[i]);
            if (value != null) {
                encodedDigest = value;
                break;
            }
        }

        if (encodedDigest == null) {
            return null;
        }

        final byte[] digest = Base64.decode(encodedDigest, Base64.DEFAULT);
        return new ManifestDigest(digest);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ManifestDigest)) {
            return false;
        }

        final ManifestDigest other = (ManifestDigest) o;

        return this == other || Arrays.equals(mDigest, other.mDigest);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mDigest);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(TO_STRING_PREFIX.length()
                + (mDigest.length * 3) + 1);

        sb.append(TO_STRING_PREFIX);

        final int N = mDigest.length;
        for (int i = 0; i < N; i++) {
            final byte b = mDigest[i];
            com.broadwave.android.java.lang.IntegralToString.appendByteAsHex(sb, b, false);
            sb.append(',');
        }
        sb.append('}');

        return sb.toString();
    }

}