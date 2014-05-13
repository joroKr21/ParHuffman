package compr.par.huff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Georgi Krastev <joro.kr.21@gmail.com>
 */
public class Counter implements Callable<Void> {

    public static final int BUF = 64 * 1024; // 64 KB
    private final File fin;
    private final int id, total;
    private final AtomicLong[] freqTable;

    public Counter(File fin, int id, int total, AtomicLong[] freqTable) {
        this.fin = fin;
        this.id = id;
        this.total = total;
        this.freqTable = freqTable;
    }

    @Override
    public Void call() throws IOException {
        InputStream in = new FileInputStream(fin);
        try {
            long len = fin.length() / total, off = id * len;
            len += id < total - 1 ? 0 : fin.length() % total;
            long[] lcl = new long[ByteSym.RANGE];
            byte[] buf = new byte[BUF];
            in.skip(off);
            while (len > 0) {
                int l = in.read(buf, 0, (int) Math.min(len, BUF));
                len -= l;
                for (int i = 0; i < l; i++) {
                    lcl[ByteSym.uByte(buf[i])]++;
                }
            }

            int sym = id * ByteSym.RANGE / total;
            for (int i = 0; i < ByteSym.RANGE; i++) {
                sym = ++sym % ByteSym.RANGE;
                if (lcl[sym] > 0) {
                    freqTable[sym].addAndGet(lcl[sym]);
                }
            }

            return null;
        } finally {
            in.close();
        }
    }
}
