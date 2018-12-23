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

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < signature.length / 4; i++) {
                long v = 0;
                if (signature[i]) {
                    v = (i + 1) * LARGE_PRIME;
                }

                // current stage
                int j = Math.min(i / rows, stages - 1);
                acc[j] = (acc[j] + v) % Integer.MAX_VALUE;
            }
        });
        Thread thread2 = new Thread(() -> {
            for (int i = signature.length / 4; i < signature.length / 2; i++) {
                long v = 0;
                if (signature[i]) {
                    v = (i + 1) * LARGE_PRIME;
                }

                // current stage
                int j = Math.min(i / rows, stages - 1);
                acc[j] = (acc[j] + v) % Integer.MAX_VALUE;
            }
        });
        Thread thread3 = new Thread(() -> {
            for (int i = signature.length / 2; i < 3 * signature.length / 4; i++) {
                long v = 0;
                if (signature[i]) {
                    v = (i + 1) * LARGE_PRIME;
                }

                // current stage
                int j = Math.min(i / rows, stages - 1);
                acc[j] = (acc[j] + v) % Integer.MAX_VALUE;
            }
        });
        Thread thread4 = new Thread(() -> {
            for (int i = 3 * signature.length / 4; i < signature.length; i++) {
                long v = 0;
                if (signature[i]) {
                    v = (i + 1) * LARGE_PRIME;
                }

                // current stage
                int j = Math.min(i / rows, stages - 1);
                acc[j] = (acc[j] + v) % Integer.MAX_VALUE;
            }
        });

        SuperBit.runAndJoinThreads(thread1, thread2, thread3, thread4);

        int[] r = new int[stages];
        for (int i = 0; i < stages; i++) {
            r[i] = (int) (acc[i] % buckets);
        }

        return r;
    }
}