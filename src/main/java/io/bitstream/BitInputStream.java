package io.bitstream;

import java.io.*;

/**
 * Reads bits-at-a-time where the number of bits is between 1 and 32. Updated
 * for version 2.0 to extend java.io.InputStream. This class can be used
 * together with <code>BitOutputStream</code> to facilitate reading and writing
 * data several bits-at-a-time. BitInputStream objects that are constructed from
 * a File support <code>reset()</code>. However, if constructed from an
 * <code>InputStream</code> an object cannot be reset.
 * <P>
 * Any exceptions generated are rethrown as <code>RuntimeException</code>
 * objects so client code does not have to catch or rethrow them. (Unless the
 * extension of <code>InputStream</code> requires throwing as another type of
 * exception, e.g., as with method <code>read</code>.
 * <P>
 *
 * @author Owen Astrachan
 * @version 1.0, July 2000
 * @version 2.0, October 2004
 */
public class BitInputStream extends InputStream {

    private InputStream in;
    private File file;
    private int bits, buf;
    public static final int[] bitMask = {
        0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff,
        0x1ff, 0x3ff, 0x7ff, 0xfff, 0x1fff, 0x3fff, 0x7fff, 0xffff,
        0x1ffff, 0x3ffff, 0x7ffff, 0xfffff, 0x1fffff, 0x3fffff,
        0x7fffff, 0xffffff, 0x1ffffff, 0x3ffffff, 0x7ffffff,
        0xfffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff, 0xffffffff
    };

    /**
     * Construct a bit-at-a-time input stream from a file whose name is
     * supplied.
     *
     * @param fname is the name of the file that will be read.
     * @throws java.io.IOException
     * @throws RuntimeException if filename cannot be opened.
     */
    public BitInputStream(String fname) throws IOException {
        this(new File(fname));
    }

    /**
     * Construct a bit-at-a-time input stream from <code>file</code>.
     *
     * @param file is the File that is the source of the input
     * @throws java.io.IOException
     */
    public BitInputStream(File file) throws IOException {
        this.file = file;
        reset();
    }

    /**
     * Open a bit-at-a-time stream that reads from supplied InputStream. If this
     * constructor is used the BitInputStream is not reset-able.
     *
     * @param in is the stream from which bits are read.
     */
    public BitInputStream(InputStream in) {
        this.in = in;
        file = null;
    }

    /**
     * Return true if the stream has been initialized from a File and is thus
     * reset-able. If constructed from an InputStream it is not reset-able.
     *
     * @return true if stream can be reset (it has been constructed
     * appropriately from a File).
     */
    @Override
    public boolean markSupported() {
        return file != null;
    }

    /**
     * Reset stream to beginning. The implementation creates a new stream.
     *
     * @throws IOException if not reset-able (e.g., constructed from
     * InputStream).
     */
    @Override
    public final void reset() throws IOException {
        if (!markSupported()) {
            throw new IOException("The stream is not resettable.");
        }

        close();
        in = new BufferedInputStream(new FileInputStream(file));
        buf = 0;
        bits = 0;
    }

    /**
     * Closes the input stream.
     *
     * @throws java.io.IOException
     * @throws RuntimeException if the close fails
     */
    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }

    /**
     * Returns the number of bits requested as rightmost bits input returned
     * value, returns -1 if not enough bits available to satisfy the request.
     *
     * @param cnt is the number of bits to read and return
     * @return the value read, only rightmost <code>howManyBits</code> are
     * valid, returns -1 if not enough bits left
     * @throws java.io.IOException
     */
    public int readBits(int cnt) throws IOException {
        int val = 0;
        if (in == null) {
            return -1;
        }

        while (cnt > bits) {
            val |= (buf << (cnt - bits));
            cnt -= bits;
            if ((buf = in.read()) == -1) { // NOTE: nested assignment
                return -1;
            }

            bits = Byte.SIZE;
        }

        if (cnt > 0) {
            val |= buf >> (bits - cnt);
            buf &= bitMask[bits - cnt];
            bits -= cnt;
        }

        return val;
    }

    /**
     * Required by classes extending InputStream, returns the next byte from
     * this stream as an int value.
     *
     * @return the next byte from this stream
     * @throws java.io.IOException
     */
    @Override
    public int read() throws IOException {
        return readBits(Byte.SIZE);
    }
}
