package compact;

import com.carrotsearch.hppc.BitSet;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.math3.util.Pair;
import org.la4j.iterator.MatrixIterator;
import org.la4j.iterator.VectorIterator;
import org.la4j.matrix.SparseMatrix;

import java.io.IOException;
import java.util.*;

/**
 * Created by Patricia Fischer on 9/05/17.
 */
public class KMeansHashClustering {

    private final int numOfClusters;

    private final HashedDocuments documentVectors;

    private final Random random;

    private static double hammingDistance;


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

        List<TIntList> clusters = new ArrayList<>(centroids.size());
        for (int i = 0; i < numOfClusters; i++) {
            clusters.add(new TIntArrayList());
        }

        List<BitSet> documentHashes = documentVectors.getDocumentHashes();

        for (int row = 0; row < documentHashes.size(); row++) {
            double minDistance = Double.MAX_VALUE;
            int closesCentroidIdx = 0;

            for (int i = 0; i < centroids.size(); i++) {

                BitSet hammingBits = documentHashes.get(row);
                hammingBits.xor(centroids.get(i)); // which bits are different?
                double distance = hammingBits.cardinality(); // number of bits set to true/ Hamming distance

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
     * Find the earliest document in the cluster. Sort documents by date and
     * return doc id with earliest date.
     *
     * @param dateIds The dates by document indices
     * @param cluster The cluster of documents
     * @return The doc id of earliest date in cluster
     */
    public int earliestDoc(Map<Integer, Date> dateIds, TIntList cluster) {
        SortedSet<Pair<Integer, Date>> sortedDates = new TreeSet<>((o1, o2) -> {
            int cmp = o1.getValue().compareTo(o2.getValue());
            if (cmp == 0) {
                return o1.getKey().compareTo(o2.getKey());
            }
            return cmp;
        });

        for (int i = 0; i < cluster.size(); i++) {
            sortedDates.add(new Pair<>(cluster.get(i), dateIds.get(cluster.get(i))));
        }

        System.out.printf("Earliest document: %s, date: %s\n", sortedDates.first().getKey(), sortedDates.first().getValue());
        return sortedDates.first().getKey();
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

        long bitSetSize = documentHashes.get(0).size();
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

        for (int iter = 0; iter < 3; iter++) {
            double objective = 0;

            int[] numOfClusterElements = new int[numOfClusters];
            List<int[]> adjustedCentroids = new ArrayList<>();
            for (int i = 0; i < centroids.size(); i++) {
                adjustedCentroids.add(new int[(int) bitSetSize]);
            }

            System.out.println("Iteration...");

            // Assign vectors to their closest centroid
            for (int row = 0; row < numOfDocs; row++) {

                double minimum = Double.MAX_VALUE;
                int idx = -1;
                BitSet bitSetRow = documentHashes.get(row);

                // Compute distance between current row and centroids to find closest centroid
                for (int i = 0; i < centroids.size(); i++) {

                    BitSet hammingBits = bitSetRow;
                    hammingBits.xor(centroids.get(i));
                    double distance = hammingBits.cardinality();

                    if (distance < minimum) {
                        minimum = distance;
                        idx = i; // index of closest centroid
                    }
                }

                // Track how often bit at position bitIdx is set
                int[] rowBitFreqs = adjustedCentroids.get(idx);
                // For all set bits (true/1), add 1 to respective adjustedCentroid int[]
                for (int bitIdx = bitSetRow.nextSetBit(0); bitIdx >= 0; bitIdx = bitSetRow.nextSetBit(bitIdx+1)) {
                    rowBitFreqs[bitIdx] = rowBitFreqs[bitIdx]+1;
                    if (bitIdx == Integer.MAX_VALUE) {
                        break; // or (i+1) would overflow
                    }
                }
                adjustedCentroids.set(idx, rowBitFreqs);
                numOfClusterElements[idx]++;
                objective += minimum;
            }

            System.err.println("Recomputing centroids...");

            for (int i = 0; i < adjustedCentroids.size(); i++) {
                // Compute centroids
                int[] adjustedCentroid = adjustedCentroids.get(i);
                int clusterElements = numOfClusterElements[i];
                centroids.set(i, new BitSet(bitSetSize));
                for (int j = 0; j < adjustedCentroid.length; j++) {
                    // If bit at position j is more often set than not, set it in centroid
                    if (clusterElements > 0) {
                        if (adjustedCentroid[j] >= clusterElements/2) {
                            centroids.get(i).set(j);
                        }
                    }
                }
            }
            System.out.println("Centroids\n"+centroids);

            hammingDistance = objective / documentHashes.size();
            System.err.printf("Average hammingDistance: %s%n", hammingDistance);
        }

        return centroids;
    }
}
