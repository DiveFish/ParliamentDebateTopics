package compact;

import com.carrotsearch.hppc.BitSet;
import com.google.common.base.Preconditions;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.la4j.iterator.VectorIterator;
import org.la4j.vector.SparseVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Random projection hashing for double vectors.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class RandomProjectionHash {

    private final int bits; //number of bits correlating with number of hash vectors

    private final List<double[]> hashes;  //a list of all randomly generated hash vectors

    // Number of bits defines how many hash vectors are created
    public RandomProjectionHash(RandomGenerator generator, int vectorLength, int bits) {
        this.bits = bits;
        this.hashes = new ArrayList(); //change to array of arrays; i * vecLeng -> instead indexing in first and then in 2nd dimension
        fill(generator, hashes, vectorLength, bits);
    }

    private void fill(RandomGenerator generator, List hashes, int vectorLength, int bits) {
        GaussianRandomGenerator gaussian = new GaussianRandomGenerator(generator);
        for (int i = 0; i < bits; i++) {
            double[] hash = new double[vectorLength];
            for (int j = 0; j < vectorLength; j++) {
                hash[i] = gaussian.nextNormalizedDouble();
            }
            hashes.add(hash);
        }
    }

    public BitSet hashVector(SparseVector vector) {
        int vecLen = hashes.get(0).length;
        Preconditions.checkArgument(vector.length() == vecLen,
                String.format("Vector should have length %d, has %d", vecLen, vector.length()));

        BitSet hash = new BitSet(bits);

        for (int i = 0; i < bits; ++i) {

            double[] randomHashVec = hashes.get(i);

            //Calculate dot product between term vector and hash vector (skip 0 values)
            VectorIterator vIter = vector.nonZeroIterator();
            double dp = 0;
            while(vIter.hasNext()) {
                dp += vIter.next() * randomHashVec[vIter.index()];
            }

            //If dot product is larger than zero, set bit to 1
            if (dp >= 0) {
                hash.set(i);
            }
        }

        return hash;
    }
}