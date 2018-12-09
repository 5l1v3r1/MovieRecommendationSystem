package LSH;

abstract class LSH {

    private static final long LARGE_PRIME =  433494437;

    private int stages;
    private int buckets;

    LSH(final int stages, final int buckets) {
        this.stages = stages;
        this.buckets = buckets;
    }

    final int[] hashSignature(final boolean[] signature) {

        // Create an accumulator for each stage
        long[] acc = new long[stages];
        for (int i = 0; i < stages; i++) {
            acc[i] = 0;
        }

        // Number of rows per stage
        int rows = signature.length / stages;

        for (int i = 0; i < signature.length; i++) {
            long v = 0;
            if (signature[i]) {
                v = (i + 1) * LARGE_PRIME;
            }

            // current stage
            int j = Math.min(i / rows, stages - 1);
            acc[j] = (acc[j] + v) % Integer.MAX_VALUE;
        }

        int[] r = new int[stages];
        for (int i = 0; i < stages; i++) {
            r[i] = (int) (acc[i] % buckets);
        }

        return r;
    }
}