package LSH;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;

public class SuperBit {

    private double[][] hyperplanes;
    private static final int DEFAULT_CODE_LENGTH = 10000;

    SuperBit(final int d, final int n, final int l) {
        this(d, n, l, new Random());
    }

    private SuperBit(final int d, final int n, final int l, final Random rand) {
        if (d <= 0) {
            throw new IllegalArgumentException("Dimension d must be >= 1");
        }

        if (n < 1 || n > d) {
            throw new IllegalArgumentException(
                    "Super-Bit depth N must be 1 <= N <= d");
        }

        if (l < 1) {
            throw  new IllegalArgumentException(
                    "Number of Super-Bit L must be >= 1");
        }

        // Input: Data space dimension d, Super-Bit depth 1 <= N <= d,
        // number of Super-Bit L >= 1,
        // resulting code length K = N * L

        // Generate a random matrix H with each element sampled independently
        // from the normal distribution
        // N (0, 1), with each column normalized to unit length.
        // Denote H = [v1, v2, ..., vK].
        int code_length = n * l;

        double[][] v = new double[code_length][d];

        for (int i = 0; i < code_length; i++) {
            double[] vector = new double[d];
            for (int j = 0; j < d; j++) {
                vector[j] = rand.nextGaussian();
            }

            normalize(vector);
            v[i] = vector;
        }


        // for i = 0 to L - 1 do
        //    for j = 1 to N do
        //       w_{iN+j} = v_{iN+j}
        //       for k = 1 to j - 1 do
        //          w_{iN+j} = w_{iN+j} - w_{iN+k} w^T_{iN+k} v_{iN+j}
        //       end for
        //       wiN+j = wiN+j / | wiN+j |
        //     end for
        //   end for
        // Output: H˜ = [w1, w2, ..., wK]

        double[][] w = new double[code_length][d];
        for (int i = 0; i <= l - 1; i++) {
            for (int j = 1; j <= n; j++) {
                double[] src = v[i * n + j - 1];
                double[] dest = w[i * n + j - 1];
                java.lang.System.arraycopy(
                        src,
                        0,
                        dest,
                        0,
                        d);

                for (int k = 1; k <= (j - 1); k++) {
                    double[] v1 = w[i * n + k - 1];
                    dest = sub(
                            dest,
                            product(
                                    dotProduct(
                                            v1,
                                            src),
                                    v1));
                }

                normalize(dest);

            }
        }

        this.hyperplanes = w;
    }

    final boolean[] signature(final float[] vector) {
        boolean[] sig = new boolean[this.hyperplanes.length];
        for (int i = 0; i < this.hyperplanes.length; i++) {
            sig[i] = (dotProduct(this.hyperplanes[i], vector) >= 0);
        }
        return sig;
    }

    static float[] listToArray(ArrayList<Float> vector) {
        float[] v = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            v[i] = vector.get(i);
        }
        return v;
    }

    /* ---------------------- STATIC ---------------------- */

    /**
     * Computes the cosine similarity, computed as v1 dot v2 / (|v1| * |v2|).
     * Cosine similarity of two vectors is the cosine of the angle between them.
     * It ranges between -1 and +1
     *
     * @param v1
     * @param v2
     * @return
     */
    public static double cosineSimilarity(final double[]v1, final double[] v2) {

        return dotProduct(v1, v2) / (norm(v1) * norm(v2));
    }

    private static double[] product(final double x, final double[] v) {
        double[] r = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            r[i] = x * v[i];
        }
        return r;
    }

    private static double[] sub(final double[] a, final double[] b) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = a[i] - b[i];
        }
        return r;
    }

    private static void normalize(final double[] vector) {
        double norm = norm(vector);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }

    }

    private static double norm(final double[] v) {
        double agg = 0;

        for (double v1 : v) {
            agg += (v1 * v1);
        }

        return Math.sqrt(agg);
    }

    private static double dotProduct(final double[] v1, final double[] v2) {
        double agg = 0;

        for (int i = 0; i < v1.length; i++) {
            agg += (v1[i] * v2[i]);
        }

        return agg;
    }

    private static double dotProduct(final double[] v1, final float[] v2) {
        return dotProduct(v1, convertToDouble(v2));
    }

    public static double[] convertToDouble(final float[] values) {
        final double[] result = new double[values.length];

        IntStream.range(0, values.length).forEach(index -> result[index] = values[index]);
        return result;
    }
}