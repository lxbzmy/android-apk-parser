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

package com.broadwave.android.android.os;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * The FileDescriptor returned by {@link Parcel#readFileDescriptor}, allowing
 * you to close it when done with it.
 */
public class ParcelFileDescriptor implements Parcelable {
    private final FileDescriptor mFileDescriptor;
    private boolean mClosed;
    //this field is to create wrapper for ParcelFileDescriptor using another
    //PartialFileDescriptor but avoid invoking close twice
    //consider ParcelFileDescriptor A(fileDescriptor fd),  ParcelFileDescriptor B(A)
    //in this particular case fd.close might be invoked twice.
    private final ParcelFileDescriptor mParcelDescriptor;

    /**
     * For use with {@link #open}: if {@link #MODE_CREATE} has been supplied
     * and this file doesn't already exist, then create the file with
     * permissions such that any application can read it.
     */
    public static final int MODE_WORLD_READABLE = 0x00000001;

    /**
     * For use with {@link #open}: if {@link #MODE_CREATE} has been supplied
     * and this file doesn't already exist, then create the file with
     * permissions such that any application can write it.
     */
    public static final int MODE_WORLD_WRITEABLE = 0x00000002;

    /**
     * For use with {@link #open}: open the file with read-only access.
     */
    public static final int MODE_READ_ONLY = 0x10000000;

    /**
     * For use with {@link #open}: open the file with write-only access.
     */
    public static final int MODE_WRITE_ONLY = 0x20000000;

    /**
     * For use with {@link #open}: open the file with read and write access.
     */
    public static final int MODE_READ_WRITE = 0x30000000;

    /**
     * For use with {@link #open}: create the file if it doesn't already exist.
     */
    public static final int MODE_CREATE = 0x08000000;

    /**
     * For use with {@link #open}: erase contents of file when opening.
     */
    public static final int MODE_TRUNCATE = 0x04000000;

    /**
     * For use with {@link #open}: append to end of file while writing.
     */
    public static final int MODE_APPEND = 0x02000000;

    /**
     * Create a new ParcelFileDescriptor from a raw native fd.  The new
     * ParcelFileDescriptor holds a dup of the original fd passed in here,
     * so you must still close that fd as well as the new ParcelFileDescriptor.
     *
     * @param fd The native fd that the ParcelFileDescriptor should dup.
     *
     * @return Returns a new ParcelFileDescriptor holding a FileDescriptor
     * for a dup of the given fd.
     */
    public static ParcelFileDescriptor fromFd(int fd) throws IOException {
        FileDescriptor fdesc = getFileDescriptorFromFd(fd);
        return new ParcelFileDescriptor(fdesc);
    }

    // Extracts the file descriptor from the specified socket and returns it untouched
    private static native FileDescriptor getFileDescriptorFromFd(int fd) throws IOException;

    /**
     * Take ownership of a raw native fd in to a new ParcelFileDescriptor.
     * The returned ParcelFileDescriptor now owns the given fd, and will be
     * responsible for closing it.  You must not close the fd yourself.
     *
     * @param fd The native fd that the ParcelFileDescriptor should adopt.
     *
     * @return Returns a new ParcelFileDescriptor holding a FileDescriptor
     * for the given fd.
     */
    public static ParcelFileDescriptor adoptFd(int fd) {
        FileDescriptor fdesc = getFileDescriptorFromFdNoDup(fd);
        return new ParcelFileDescriptor(fdesc);
    }

    // Extracts the file descriptor from the specified socket and returns it untouched
    private static native FileDescriptor getFileDescriptorFromFdNoDup(int fd);


    /**
     * Create two ParcelFileDescriptors structured as a data pipe.  The first
     * ParcelFileDescriptor in the returned array is the read side; the second
     * is the write side.
     */
    public static ParcelFileDescriptor[] createPipe() throws IOException {
        FileDescriptor[] fds = new FileDescriptor[2];
        createPipeNative(fds);
        ParcelFileDescriptor[] pfds = new ParcelFileDescriptor[2];
        pfds[0] = new ParcelFileDescriptor(fds[0]);
        pfds[1] = new ParcelFileDescriptor(fds[1]);
        return pfds;
    }

    private static native void createPipeNative(FileDescriptor[] outFds) throws IOException;

    /**
     * Retrieve the actual FileDescriptor associated with this object.
     *
     * @return Returns the FileDescriptor associated with this object.
     */
    public FileDescriptor getFileDescriptor() {
        return mFileDescriptor;
    }

    /**
     * Return the total size of the file representing this fd, as determined
     * by stat().  Returns -1 if the fd is not a file.
     */
    public native long getStatSize();

    /**
     * This is needed for implementing AssetFileDescriptor.AutoCloseOutputStream,
     * and I really don't think we want it to be public.
     * @hide
     */
    public native long seekTo(long pos);

    /**
     * Return the native fd int for this ParcelFileDescriptor.  The
     * ParcelFileDescriptor still owns the fd, and it still must be closed
     * through this API.
     */
    public int getFd() {
        if (mClosed) {
            throw new IllegalStateException("Already closed");
        }
        return getFdNative();
    }

    private native int getFdNative();


    /**
     * Close the ParcelFileDescriptor. This implementation closes the underlying
     * OS resources allocated to represent this stream.
     *
     * @throws IOException
     *             If an error occurs attempting to close this ParcelFileDescriptor.
     */
    public void close() throws IOException {
        synchronized (this) {
            if (mClosed) return;
            mClosed = true;
        }
        if (mParcelDescriptor != null) {
            // If this is a proxy to another file descriptor, just call through to its
            // close method.
            mParcelDescriptor.close();
        } else {
//            Parcel.closeFileDescriptor(mFileDescriptor);
        }
    }

    /**
     * An InputStream you can create on a ParcelFileDescriptor, which will
     * take care of calling {@link ParcelFileDescriptor#close
     * ParcelFileDescriptor.close()} for you when the stream is closed.
     */
    public static class AutoCloseInputStream extends FileInputStream {
        private final ParcelFileDescriptor mFd;

        public AutoCloseInputStream(ParcelFileDescriptor fd) {
            super(fd.getFileDescriptor());
            mFd = fd;
        }

        @Override
        public void close() throws IOException {
            try {
                mFd.close();
            } finally {
                super.close();
            }
        }
    }

    /**
     * An OutputStream you can create on a ParcelFileDescriptor, which will
     * take care of calling {@link ParcelFileDescriptor#close
     * ParcelFileDescriptor.close()} for you when the stream is closed.
     */
    public static class AutoCloseOutputStream extends FileOutputStream {
        private final ParcelFileDescriptor mFd;

        public AutoCloseOutputStream(ParcelFileDescriptor fd) {
            super(fd.getFileDescriptor());
            mFd = fd;
        }

        @Override
        public void close() throws IOException {
            try {
                mFd.close();
            } finally {
                super.close();
            }
        }
    }

    @Override
    public String toString() {
        return "{ParcelFileDescriptor: " + mFileDescriptor + "}";
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!mClosed) {
                close();
            }
        } finally {
            super.finalize();
        }
    }

    public ParcelFileDescriptor(ParcelFileDescriptor descriptor) {
        super();
        mParcelDescriptor = descriptor;
        mFileDescriptor = mParcelDescriptor.mFileDescriptor;
    }

    /*package */ParcelFileDescriptor(FileDescriptor descriptor) {
        super();
        if (descriptor == null) {
            throw new NullPointerException("descriptor must not be null");
        }
        mFileDescriptor = descriptor;
        mParcelDescriptor = null;
    }

    /* Parcelable interface */
    public int describeContents() {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }

}