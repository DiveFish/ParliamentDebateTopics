package compact;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.*;
import org.apache.commons.math3.util.Pair;

import org.la4j.Vector;
import org.la4j.Matrix;
import org.la4j.vector.SparseVector;

/**
 * K-means clustering for document vectors.
 *
 * @author Patricia Fischer
 */
public class KMeansClustering {

    private final int numOfClusters;

    private final Matrix documentVectors;

    private final Random random;
    
    private static double cosine;

    private final int NUM_OF_ITER = 3;


    public KMeansClustering(int numOfClusters, Matrix documentVectors, Random random) {
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
        
        double maximum = -Double.MAX_VALUE;
        List<TIntList> bestCluster = new ArrayList<>();
        List<TIntList> cluster;
        for (int i = 0; i < n; i++) {
            cluster = clusters(centroids());
            if (cosine > maximum) {
                maximum = cosine;
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
    public List<TIntList> clusters(List<Vector> centroids) {

        List<TIntList> clusters = new ArrayList<>(centroids.size());
        for (int i = 0; i < numOfClusters; i++) {
            clusters.add(new TIntArrayList());
        }

        for (int row = 0; row < documentVectors.rows(); row++) {
            double maxSimilarity = -Double.MAX_VALUE;
            int closesCentroidIdx = 0;

            for (int i = 0; i < centroids.size(); i++) {
                Vector centroid = centroids.get(i);

                double similarity = documentVectors.getRow(row).innerProduct(centroid);
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
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
     * @throws java.io.IOException
     */

    public List<Vector> centroids() throws IOException {

        if (documentVectors.max() == 0) {
            throw new IOException("Trying to extract clusters from zero matrix");
        }

        if (documentVectors.rows() < numOfClusters) {
            throw new IOException("Trying to extract more clusters than matrix has elements");
        }

        int vectorLength = documentVectors.columns();  //vocab size
        int numOfDocs = documentVectors.rows();

        normalizeDocumentVectors(documentVectors);

        // Choose random centroids from document vectors
        Set<Integer> seedDocs = new HashSet<>();
        while (seedDocs.size() < numOfClusters) {
            seedDocs.add(this.random.nextInt(numOfDocs));
        }

        List<Vector> centroids = new ArrayList<>();
        for (int doc : seedDocs) {
            centroids.add(documentVectors.getRow(doc));
        }

        for (int iter = 0; iter < NUM_OF_ITER; iter++) {
            double objective = 0;
            TDoubleList cosineSimilarities = new TDoubleArrayList();

            int[] numOfClusterElements = new int[numOfClusters];
            List<Vector> adjustedCentroids = new ArrayList<>();
            for (int i = 0; i < numOfClusters; i++) {
                adjustedCentroids.add(SparseVector.zero(vectorLength));
            }

            System.err.println("Iteration...");

            // Assign vectors to their closest centroid
            for (int row = 0; row < numOfDocs; row++) {

                double maximum = -Double.MAX_VALUE;
                int idx = -1;
                for (int i = 0; i < centroids.size(); i++) {
                    double similarity = documentVectors.getRow(row).innerProduct(centroids.get(i));
                    if (similarity > maximum) {
                        maximum = similarity;
                        idx = i; // index of closest centroid
                    }
                }
                // add vector at idx of closest centroid -> "assign" vector to centroid
                adjustedCentroids.set(idx, adjustedCentroids.get(idx).add(documentVectors.getRow(row)));
                numOfClusterElements[idx]++;
                objective += maximum;
                cosineSimilarities.add(maximum);
            }

            System.err.println("Recomputing centroids...");

            for (int idx = 0; idx < adjustedCentroids.size(); idx++) {
                // Compute centroid and normalize to unit vector.
                SparseVector unnormalizedCentroid = adjustedCentroids.get(idx).divide(numOfClusterElements[idx]).toSparseVector();
                adjustedCentroids.set(idx, unnormalizedCentroid.divide(norm(unnormalizedCentroid)));
            }

            centroids = adjustedCentroids;
            cosine = objective / documentVectors.rows();
            System.out.printf("Average cosine similarity %s: %s%n", iter+1, cosine);
            if (iter == NUM_OF_ITER-1) {
                System.out.println("Single cosine similarities:");
                for (int i = 0; i < cosineSimilarities.size(); i++) {
                    System.out.println(cosineSimilarities.get(i));
                }
                System.out.println();
            }
        }

        return centroids;
    }

    private double norm(SparseVector v) {
        // Euclidean norm in la4j uses BigDecimal, which has a lot of overhead.
        return Math.sqrt(v.innerProduct(v));
    }

    private void normalizeDocumentVectors(Matrix documentVectors) {
        for (int i = 0; i < documentVectors.rows(); i++) {
            SparseVector doc = documentVectors.getRow(i).toSparseVector();
            documentVectors.setRow(i, doc.divide(norm(doc)));
        }
    }
}
