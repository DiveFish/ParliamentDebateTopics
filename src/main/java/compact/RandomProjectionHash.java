package compact;

import com.carrotsearch.hppc.BitSet;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.la4j.Vector;
import org.la4j.vector.SparseVector;

/**
 * Random projection hashing for integer vectors.
 *
 * @author Daniël de Kok &lt;me@danieldk.eu&gt;
 */
public class RandomProjectionHash {
    private final int bits;

    private final double[] hashes;

    public RandomProjectionHash(RandomGenerator generator, int vectorLength, int bits) {
        this.bits = bits;
        this.hashes = new double[bits * vectorLength];
        fill(generator, hashes);
    }

    private void fill(RandomGenerator generator, double[] hashes) {
        GaussianRandomGenerator gaussian = new GaussianRandomGenerator(generator);

        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = gaussian.nextNormalizedDouble();
        }
    }

    public BitSet hashVector(SparseVector vector) {
        int vecLen = hashes.length / bits;
        Preconditions.checkArgument(vector.length() == vecLen,
                String.format("Vectors should have length %d, has %d", vecLen, vector.length()));

        BitSet hash = new BitSet(bits);

        for (int i = 0; i < bits; ++i) {
            final MutableDouble dp = new MutableDouble();
            final int idx = i;
/*
            vector.set();
            //function calls for every non-zero component of vector
            //computing dot-product
            //current non-zero vector component
            //non-zero compontent of
            //j is component

            //call function for every non-zero vector component
            vector.applySparse((Integer j, Double val) -> {
                dp.add(val * hashes[idx * vecLen + j]); //gives right vector representing hash; j gives compontent of vector
                return val;
            });
*/
            //if result is larger than zero, set bit to 1
            if (dp.doubleValue() >= 0) {
                hash.set(i);
            }
        }

        return hash;
    }


    public BitSet hashVector(Vector vector) {
        int vecLen = hashes.length / bits;
        Preconditions.checkArgument(vector.length() == vecLen,
                String.format("Vectors should have length %d, has %d", vecLen, vector.length()));

        BitSet hash = new BitSet(bits);

        for (int i = 0; i < bits; ++i) {
            double dp = 0;
            for (int j = 0; j < vecLen; j++) {
                dp += vector.get(j) * hashes[i * vecLen + j];
            }

            if (dp >= 0) {
                hash.set(i);
            }
        }

        return hash;
    }
}