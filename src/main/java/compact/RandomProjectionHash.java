package compact;

import com.carrotsearch.hppc.BitSet;
import com.google.common.base.Preconditions;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.la4j.Vector;
import org.la4j.iterator.VectorIterator;
import org.la4j.vector.SparseVector;

/**
 * Random projection hashing for double vectors.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class RandomProjectionHash {

    private final int bits; //number of bits correlating with number of hash vectors

    private final double[] hashes;  //a 1D-array containing all randomly generated hash vectors

    // Number of bits defines how many hash vectors are created
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
                String.format("Vector should have length %d, has %d", vecLen, vector.length()));

        BitSet hash = new BitSet(bits);

        for (int i = 0; i < bits; ++i) {
            //Calculate dot product between term vector and hash vector (skip 0 values)
            VectorIterator vIter = vector.nonZeroIterator();
            double dp = 0;
            while(vIter.hasNext()) {
                dp += vIter.next() * hashes[i * vecLen + vIter.index()];  //i*vecLen -> get to hashes part of current vector; vIter.index() -> current vector component
            }

            //If dot product is larger than zero, set bit to 1
            if (dp >= 0) {
                hash.set(i);
            }


            //function calls for every non-zero component of vector
            //computing dot-product
            //current non-zero vector component j
            //call function for every non-zero vector component

            /*
            vector.applySparse((Integer j, Double val) -> {
                dp.add(val * hashes[idx * vecLen + j]); //gives right vector representing hash; j gives compontent of vector
                return val;
            });
            */
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