package compr.par.huff;

/**
 * @author Georgi Krastev <joro.kr.21@gmail.com>
 */
public class ByteSym implements Comparable<ByteSym> {

    public static final int RANGE = Byte.MAX_VALUE - Byte.MIN_VALUE + 2;
    public static final int MIN = 0, MAX = RANGE - 2, EoF = RANGE - 1;
    private final int val;
    private final byte bits;

    public static int uByte(byte val) {
        return val & 0xFF;
    }

    public ByteSym(int val, byte bits) {
        this.val = val;
        this.bits = bits;
    }

    @Override
    public int compareTo(ByteSym o) {
        int b = ((Byte) getBits()).compareTo(o.getBits());
        return b != 0 ? b : ((Integer) intVal()).compareTo(o.intVal());
    }

    public int intVal() {
        return val;
    }

    public byte getBits() {
        return bits;
    }
}
