package io.bitstream;

/**
 * write bits-at-a-time where the number of bits is between 1 and 32 Client
 * programs must call <code>flush</code> or <code>close</code> when finished
 * writing or not all bits will be written.
 * <P>
 * Updated for version 2.0 to extend java.io.OutputStream
 *
 * @author Owen Astrachan
 * @version 1.0, July 2000
 * @version 2.0, October 2004
 */
import java.io.*;

public class BitOutputStream extends OutputStream {

    private OutputStream out;
    private int buf, bits;
    public static final int[] bitMask = {
        0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff,
        0x1ff, 0x3ff, 0x7ff, 0xfff, 0x1fff, 0x3fff, 0x7fff, 0xffff,
        0x1ffff, 0x3ffff, 0x7ffff, 0xfffff, 0x1fffff, 0x3fffff,
        0x7fffff, 0xffffff, 0x1ffffff, 0x3ffffff, 0x7ffffff,
        0xfffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff, 0xffffffff
    };

    /**
     * Required by OutputStream subclasses, write the low 8-bits to the
     * underlying output stream
     * @throws java.io.IOException
     */
    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    public BitOutputStream(OutputStream out) {
        this.out = out;
        buf = 0;
        bits = Byte.SIZE;
    }

    /**
     * Construct a bit-at-a-time output stream with specified file name
     *
     * @param fname is the name of the file being written
     * @throws java.io.IOException
     */
    public BitOutputStream(String fname) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(fname)));
    }

    /**
     * Flushes bits not yet written, must be called by client programs if
     * <code>close</code> isn't called.
     *
     * @throws java.io.IOException
     */
    @Override
    public void flush() throws IOException {
        if (bits != Byte.SIZE) {
            write((buf << bits));
            buf = 0;
            bits = Byte.SIZE;
        }

        out.flush();
    }

    /**
     * releases system resources associated with file and flushes bits not yet
     * written. Either this function or flush must be called or not all bits
     * will be written
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }

    /**
     * write bits to file
     *
     * @param cnt is number of bits to write (1-32)
     * @param val is source of bits, rightmost bits are written
     * @throws java.io.IOException
     */
    public void write(int cnt, int val) throws IOException {
        val &= bitMask[cnt];  // only right most bits valid
        while (cnt >= bits) {
            buf = (buf << bits) | (val >> (cnt - bits));
            write(buf);
            val &= bitMask[cnt - bits];
            cnt -= bits;
            bits = Byte.SIZE;
            buf = 0;
        }

        if (cnt > 0) {
            buf = (buf << cnt) | val;
            bits -= cnt;
        }
    }
}
