package compact;

import com.carrotsearch.hppc.BitSet;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.util.*;

/**
 * K-means clustering for bit vectors.
 *
 * @author Patricia Fischer
 */
public class KMeansHashClustering {

    private final int numOfClusters;

    private final HashedDocuments documentVectors;

    private final Random random;

    private static double hammingDistance;

    private final int NUM_OF_ITER = 5;

    public KMeansHashClustering(int numOfClusters, HashedDocuments documentVectors, Random random) {
        this.numOfClusters = numOfClusters;
        this.documentVectors = documentVectors;
        this.random = random;
    }

    /**
     * Create several clusters to decide for best one based on cosine similarity
     * of vectors to centroids.
     *
     * @param n Number of cluster calculations
     * @return Best clustering
     * @throws IOException
     */
    public List<TIntList> nClusters(Integer n) throws IOException {

        double minimum = Double.MAX_VALUE;
        List<TIntList> bestCluster = new ArrayList<>();
        List<TIntList> cluster;
        for (int i = 0; i < n; i++) {
            cluster = clusters(centroids());
            if (hammingDistance < minimum) {
                minimum = hammingDistance;
                bestCluster = cluster;
            }
        }

        return bestCluster;
    }

    /**
     * Calculate clusters for matrix from a given list of centroids. Compare
     * the vectors in the matrix to the centroids and assign vectors to closest
     * centroid. Each cluster contains row indices of the doc vectors in the cluster.
     *
     * @param centroids The list of centroid vectors
     * @return The clusters as list of vector idxs
     */
    public List<TIntList> clusters(List<BitSet> centroids) {

        double bitSetSize = documentVectors.getHashLength();

        List<TIntList> clusters = new ArrayList<>(centroids.size());
        for (int i = 0; i < numOfClusters; i++) {
            clusters.add(new TIntArrayList());
        }

        List<BitSet> documentHashes = documentVectors.getDocumentHashes();

        for (int row = 0; row < documentHashes.size(); row++) {
            double minDistance = Double.MAX_VALUE;
            int closesCentroidIdx = 0;

            for (int i = 0; i < centroids.size(); i++) {

                BitSet hammingBits = (BitSet) documentHashes.get(row).clone();
                hammingBits.xor(centroids.get(i)); // which bits are different?
                double distance = (hammingBits.cardinality()/bitSetSize); // number of bits set to true/ Hamming distance

                if (distance < minDistance) {
                    minDistance = distance;
                    closesCentroidIdx = i;
                }
            }

            clusters.get(closesCentroidIdx).add(row);
        }

        return clusters;
    }

    /**
     * Cluster data by k-means clustering. Calculate euclidean distance from all
     * document vectors to all centroids, assign each to their closest centroid.
     * When all documents are assigned to a centroid, readjust centroids.
     *
     * @return The clusters
     * @throws IOException
     */

    public List<BitSet> centroids() throws IOException {

        List<BitSet> documentHashes = documentVectors.getDocumentHashes();

        if (documentHashes.isEmpty()) {
            throw new IOException("Trying to extract clusters from zero matrix");
        }

        if (documentHashes.size() < numOfClusters) {
            throw new IOException("Trying to extract more clusters than matrix has elements");
        }

//        int bitSetSize = (int) documentHashes.get(0).size();
        int bitSetSize = documentVectors.getHashLength();
        int numOfDocs = documentHashes.size();

        // Choose random centroids from document vectors
        Set<Integer> seedDocs = new HashSet<>();
        while (seedDocs.size() < numOfClusters) {
            seedDocs.add(this.random.nextInt(numOfDocs));
        }

        List<BitSet> centroids = new ArrayList<>();
        for (int doc : seedDocs) {
            centroids.add(documentHashes.get(doc));
        }

        for (int iter = 0; iter < NUM_OF_ITER; iter++) {
            double objective = 0;
            TDoubleList hammingDistances = new TDoubleArrayList();

            int[] numOfClusterElements = new int[numOfClusters];
            List<int[]> adjustedCentroids = new ArrayList<>();
            for (int i = 0; i < centroids.size(); i++) {
                adjustedCentroids.add(new int[bitSetSize]);
            }

            System.out.println("Iterating...");

            // Assign vectors to their closest centroid
            for (int row = 0; row < numOfDocs; row++) {

                double minimum = Double.MAX_VALUE;
                int idx = -1;
                BitSet bitSetRow = documentHashes.get(row); //the current row

                // Compute distance between current row and centroids to find closest centroid
                for (int i = 0; i < centroids.size(); i++) {

                    BitSet hammingBits = (BitSet) bitSetRow.clone();
                    hammingBits.xor(centroids.get(i));
                    double distance = (hammingBits.cardinality() / (double) bitSetSize);

                    if (distance < minimum) {
                        minimum = distance;
                        idx = i; // index of closest centroid
                    }
                }

                // Track how often bit at position bitIdx is set to 1
                int[] rowBitFreqs = adjustedCentroids.get(idx);
                // For all set bits (true/1), add 1 to respective adjustedCentroid int[]
                for (int bitIdx = bitSetRow.nextSetBit(0); bitIdx >= 0; bitIdx = bitSetRow.nextSetBit(bitIdx+1)) {
                    rowBitFreqs[bitIdx]++;
                    if (bitIdx == Integer.MAX_VALUE) {
                        break; // or (i+1) would overflow
                    }
                }
                numOfClusterElements[idx]++;
                objective += minimum;
                hammingDistances.add(minimum);
            }

            System.err.println("Recomputing centroids...");

            for (int i = 0; i < adjustedCentroids.size(); i++) {
                // Compute centroids
                int[] adjustedCentroid = adjustedCentroids.get(i);
                int clusterElements = numOfClusterElements[i];
                centroids.set(i, new BitSet(bitSetSize));
                for (int j = 0; j < adjustedCentroid.length; j++) {
                    // If bit at position j is more often set to 1 than not, set it in centroid (ties go towards 0)
                    if (clusterElements > 0) {
                        if (adjustedCentroid[j] > clusterElements/2) { //int tieCounter: increase whenever there is a tie; once set bit to 1, other time set bit to 0
                            centroids.get(i).set(j);
                        }
                    }
                }
            }
            System.out.println(documentHashes.size());
            hammingDistance = objective / numOfDocs;
            System.out.printf("Average hammingDistance %s: %s%n", iter+1, hammingDistance);
            if (iter == NUM_OF_ITER-1) {
                System.out.printf("Single hamming distances ("+ bitSetSize+"):\n"+hammingDistances.toString()+"\n");
            }
        }

        return centroids;
    }
}
