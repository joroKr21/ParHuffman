package compr.par.huff;

/**
 * @author Georgi Krastev <joro.kr.21@gmail.com>
 */
public class HuffNode implements Comparable<HuffNode> {

    private int sym;
    private byte bits;
    private long weight;
    private final boolean leaf;
    public HuffNode lo, hi;

    public HuffNode(int sym, long weight) {
        this.sym = sym;
        this.weight = weight;
        leaf = true;
    }

    public HuffNode(HuffNode lo, HuffNode hi) {
        this.lo = lo;
        this.hi = hi;
        leaf = false;
        if (lo != null && hi != null) {
            weight = lo.getWeight() + hi.getWeight();
        }
    }

    @Override
    public int compareTo(HuffNode node) {
        if (equals(node)) {
            return 0;
        } else if (getWeight() < node.getWeight()) {
            return -1;
        } else if (getWeight() > node.getWeight()) {
            return 1;
        } else {
            if (!isLeaf() && node.isLeaf()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public boolean isLeaf() {
        return leaf;
    }

    public int getSym() {
        return sym;
    }

    public long getWeight() {
        return weight;
    }

    public byte getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = (byte) bits;
    }
}
