package LSH;

import java.util.ArrayList;

public class LSHSuperBit extends LSH {

    private SuperBit sb;

    public LSHSuperBit(
            final int stages, final int buckets, final int dimensions) {

        super(stages, buckets);

        int code_length = stages * buckets / 2;
        int superbit = computeSuperBit(stages, buckets, dimensions);

        this.sb = new SuperBit(dimensions, superbit, code_length / superbit);
    }

    private int computeSuperBit(
            final int stages,  final int buckets, final int dimensions) {

        // SuperBit code length
        int code_length = stages * buckets / 2;
        int superbit; // superbit value
        for (superbit = dimensions; superbit >= 1; superbit--) {
            if (code_length % superbit == 0) {
                break;
            }
        }

        if (superbit == 0) {
            throw new IllegalArgumentException(
                    "Superbit is 0 with parameters: s=" + stages
                            + " b=" + buckets + " n=" + dimensions);
        }

        return superbit;
    }

    public int[] hash(final float[] vector) {
        return hashSignature(sb.signature(vector));
    }

    //Hash a vector in s bands into b buckets.
    public final int[] hash(final ArrayList<Float> vector) {
        float[] d = SuperBit.listToArray(vector);
        return hash(d);
    }

    public SuperBit getSb() {
        return sb;
    }
}