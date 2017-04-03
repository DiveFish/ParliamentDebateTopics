package ujmp_trial;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.*;
import org.apache.commons.math3.util.Pair;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.doublematrix.SparseDoubleMatrix2D;
import org.ujmp.core.objectmatrix.impl.ObjectCalculationMatrix;
import org.ujmp.core.util.MathUtil;
import org.ujmp.core.util.VerifyUtil;

/**
 * K-means clustering for document vectors.
 *
 * @author Patricia Fischer
 */
public class KMeansClustering {

    private final int numOfClusters;

    private final SparseDoubleMatrix2D documentVectors;

    private final Random random;
    
    private static double cosine;


    public KMeansClustering(int numOfClusters, SparseDoubleMatrix2D documentVectors, Random random) {
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
    public List<TIntList> clusters(SparseDoubleMatrix2D centroids) {

        int centroidSize = (int) centroids.getRowCount();
        List<TIntList> clusters = new ArrayList(centroidSize);
        for (int i = 0; i < numOfClusters; i++) {
            clusters.add(new TIntArrayList());
        }

        for (int row = 0; row < documentVectors.getRowCount(); row++) {
            double maxSimilarity = -Double.MAX_VALUE;
            int closesCentroidIdx = 0;

            for (int i = 0; i < centroidSize; i++) {
                ObjectCalculationMatrix sgCentroid = (ObjectCalculationMatrix) centroids.selectRows(Calculation.Ret.LINK, i);
                //double similarity = sgCentroid.cosineSimilarityTo(documentVectors.selectRows(Calculation.Ret.LINK, row), true);
                double similarity = getCosineSimilarity(sgCentroid, documentVectors.selectRows(Calculation.Ret.LINK, row), true);
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    closesCentroidIdx = i; // index of closest centroid
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

    public SparseDoubleMatrix2D centroids() throws IOException {

        if (documentVectors.getMaxValue() == 0) {
            throw new IOException("Trying to extract clusters from zero matrix");
        }

        if (documentVectors.getRowCount()< numOfClusters) {
            throw new IOException("Trying to extract more clusters than matrix has elements");
        }

        int vectorLength = (int) documentVectors.getColumnCount();  //vocab size
        int numOfDocs = (int) documentVectors.getRowCount();

        // Choose random centroids from document vectors
        List<Long> seedDocs = new ArrayList();
        while (seedDocs.size() < numOfClusters) {
            seedDocs.add((long) this.random.nextInt(numOfDocs));
        }

        SparseDoubleMatrix2D centroids = (SparseDoubleMatrix2D) documentVectors.selectRows(Calculation.Ret.NEW, seedDocs);

        for (int iter = 0; iter < 1; iter++) { //TODO: change back to several iterations after testing
            double objective = 0;

            int[] numOfClusterElements = new int[numOfClusters];
            // list of row idxs -> select rows with these idxs
            List<List<Long>> adjustedCentroids = new ArrayList<>();
            for (int i = 0; i < numOfClusters; i++) {
                adjustedCentroids.add(new ArrayList());
            }

            System.err.println("iteration...");

            // Assign vectors to their closest centroid
            for (int row = 0; row < numOfDocs; row++) {

                double maximum = -Double.MAX_VALUE;
                int idx = -1;
                for (int i = 0; i < centroids.getRowCount(); i++) {
                    ObjectCalculationMatrix centroid = (ObjectCalculationMatrix) centroids.selectRows(Calculation.Ret.LINK, i);
                    
                    //double similarity = centroid.cosineSimilarityTo(documentVectors.selectRows(Calculation.Ret.LINK, row), true);
                    double similarity = getCosineSimilarity(centroid, documentVectors.selectRows(Calculation.Ret.LINK, row), true);
                    if (similarity > maximum) {
                        maximum = similarity;
                        idx = i; // index of closest centroid
                    }
                }
                // Add row to "row idx list" at idx of closest centroid -> "assign" vector to centroid
                adjustedCentroids.get(idx).add((long) row);
                numOfClusterElements[idx]++;
                objective += maximum;
            }

            System.err.println("Recomputing centroids...");
            
            centroids = SparseDoubleMatrix2D.Factory.zeros(0, vectorLength);

            for (int idx = 0; idx < adjustedCentroids.size(); idx++) {
                centroids = (SparseDoubleMatrix2D) centroids.appendVertically(Calculation.Ret.NEW, documentVectors.selectRows(Calculation.Ret.LINK, adjustedCentroids.get(idx)).sum(Calculation.Ret.NEW, 0, true).divide(numOfClusterElements[idx]));
            }

            cosine = objective / numOfDocs;
            System.err.printf("Average cosine similarity: %s%n", cosine);
        }

        return centroids;
    }

    private void normalizeMatrix(Matrix m) {
        double length = 0;
        int rowNum = 0;
        for (long[] coords : m.nonZeroCoordinates()){
            // Reset values and calculate norm on preceding row when moving to next row
            if (coords[0] > rowNum) {
                m.selectRows(Calculation.Ret.ORIG, rowNum).divide(Math.sqrt(length));
                rowNum++;
                length = 0;
            }
            double val = m.getAsDouble(coords);
            length += (val*val);
        }
        //Matrix norm =  m.times(m).sum(Calculation.Ret.NEW, 1, true).sqrt(Calculation.Ret.NEW);
        //m.divide(norm);
    }

    /**
     * Find the earliest document in the cluster. Sort documents by date and
     * return doc id with earliest date.
     *
     * @param dateIds
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
     * UJMP CosineSimilarity method. In original called "getCosineSimilartiy" (typo)
     * @param m1 First matrix
     * @param m2 Second matrix
     * @param ignoreNaN
     * @return Cosine similarity between the two matrices
     */
    private static double getCosineSimilarity(Matrix m1, Matrix m2, boolean ignoreNaN) {
        VerifyUtil.verifySameSize(m1, m2);

        double aiSum = 0;
        double a2Sum = 0;
        double b2Sum = 0;

        //Iterator<long[]> it1 = m1.selectRows(Calculation.Ret.ORIG, 0).divide(m1.norm2()).nonZeroCoordinates().iterator();
        //Iterator<long[]> it2 = m2.selectRows(Calculation.Ret.ORIG, 0).divide(m2.norm2()).nonZeroCoordinates().iterator();
        Iterator<long[]> it1 = m1.selectRows(Calculation.Ret.LINK, 0).nonZeroCoordinates().iterator();
        Iterator<long[]> it2 = m2.selectRows(Calculation.Ret.LINK, 0).nonZeroCoordinates().iterator();

        //Get norm

        int col1 = (int) it1.next()[1];
        int col2 = (int) it2.next()[1];

        boolean endOfMatrix1 = false;
        boolean endOfMatrix2 = false;

        while (!(endOfMatrix1 && endOfMatrix2)) {

            //Non-zero value in same column
            if (col1 == col2) {
                double a = m1.getAsDouble(0, col1);
                double b = m2.getAsDouble(0, col2);
                aiSum += a * b;
                a2Sum += a * a;
                b2Sum += b * b;
                if (! (it1.hasNext() && it2.hasNext())) {
                    break;
                }
                else if (it1.hasNext()) {
                    col1 = (int) it1.next()[1];
                    endOfMatrix2 = true;
                }
                else if (it2.hasNext()) {
                    col2 = (int) it2.next()[1];
                    endOfMatrix1 = true;
                }
            }
            // Column in m1 lower than in m2 -> move m1
            else if (col1 < col2) {
                double a = m1.getAsDouble(0, col1);
                a2Sum += a * a;
                if (it1.hasNext()) {
                    col1 = (int) it1.next()[1];
                }
                else {
                    endOfMatrix1 = true;
                }
            }
            // Column in m2 lower than in m1 -> move m2
            else if (col1 > col2) {
                double b = m2.getAsDouble(0, col2);
                b2Sum += b * b;
                if (it2.hasNext()) {
                    col2 = (int) it2.next()[1];
                }
                else {
                    endOfMatrix2 = true;
                }
            }
        }

        /*
        for (long[] c : m1.allCoordinates()) {
            double a = m1.getAsDouble(c);
            double b = m2.getAsDouble(c);
            if (ignoreNaN) {
                if (!MathUtil.isNaNOrInfinite(a) && !MathUtil.isNaNOrInfinite(b)) {
                    aiSum += a * b;
                    a2Sum += a * a;
                    b2Sum += b * b;
                }
            } else {
                aiSum += a * b;
                a2Sum += a * a;
                b2Sum += b * b;
            }
        }
        */
        return aiSum / (Math.sqrt(a2Sum) * Math.sqrt(b2Sum));
    }
}
