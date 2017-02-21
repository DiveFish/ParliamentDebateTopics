package compact;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static matrix.VectorMeasures.euclideanDistance;
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
    
    
    public KMeansClustering(int numOfClusters, Matrix documentVectors) {
        this.numOfClusters = numOfClusters;
        this.documentVectors = documentVectors;
    }
    
    /**
     * Create several clusters to decide for best one.
     *
     * @param n Number of cluster calculations
     * @return
     * @throws IOException
     */
    public List<List<TIntList>> nClusters(Integer n) throws IOException {
        //TODO: pick best cluster; calculate best centroids and then retrieve clusters?
        List<List<TIntList>> clusterColl = new ArrayList();
        for (int i = 0; i < n; i++) {
            clusterColl.add(clusters(centroids()));
        }
        return clusterColl;
    }
    
    /**
     * Calculate clusters for matrix from a given list of centroids. Compare
     * the vectors in the matrix to the centroids and assign vectors to closest
     * centroid.
     *
     * @param centroids The list of centroid vectors
     * @return The clusters as list of vector lists
     */
    public List<TIntList> clusters(List<Vector> centroids) {
        
        List<TIntList> clusters = new ArrayList<>(centroids.size());
        for (int i = 0; i < numOfClusters; i++) {
            clusters.add(new TIntArrayList());
        }
        
        for (int row = 0; row < documentVectors.rows(); row++) {
            double minimum = Double.MAX_VALUE;
            Vector closestCentroid = SparseVector.zero(documentVectors.columns());

            for (Vector centroid : centroids) {
                double euc = euclideanDistance(documentVectors.getRow(row), centroid);
                if (euc < minimum) {
                    minimum = euc;
                    closestCentroid = centroid;
                }
            }
            clusters.get(centroids.indexOf(closestCentroid)).add(row);
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
        
        int vector_length = documentVectors.columns();  //vocab size
        int num_of_docs = documentVectors.rows();
        
        List<Vector> centroids = new ArrayList<>();
        
        // Choose random centroids from document vectors
        Random rand = new Random(28);
        while (centroids.size() < numOfClusters) {
            int randomRow = rand.nextInt(num_of_docs);
            if (!centroids.contains(documentVectors.getRow(randomRow))) {
                centroids.add(documentVectors.getRow(randomRow));
            }
        }
        
        // parallel indexing for centroids, adjustedCentroids and numOfClusterElements
        // -> centroid; doc vectors closest to this centroid; number of doc vectors assigned to centroid
        List<Vector> adjustedCentroids = new ArrayList();
        int[] numOfClusterElements = new int[numOfClusters];  
        
        while (adjustedCentroids.size() < numOfClusters) {
            adjustedCentroids.add(SparseVector.zero(vector_length));
        }
        
        boolean converged = false;
        while (!converged) {
            
            // Assign vectors to their closest centroid
            for (int row = 0; row < num_of_docs; row++) {
                double minimum = Double.MAX_VALUE;
                
                int idx = -1;
                for (int i = 0; i < centroids.size(); i++) {
                    double euc = euclideanDistance(documentVectors.getRow(row), centroids.get(i));
                    if (euc < minimum) {
                        minimum = euc;
                        idx = i; // index of closest centroid 
                    }
                }
                // add vector at idx of closest centroid -> "assign" vector to centroid
                adjustedCentroids.set(idx, adjustedCentroids.get(idx).add(documentVectors.getRow(row)));
                numOfClusterElements[idx]++;
            }
            
            // Distance between old and recomputed centroids
            double distOfCentr = 0;
            for (int idx = 0; idx < adjustedCentroids.size(); idx++) {
                
                // divide sum of cluster vectors by number of vectors in this cluster
                adjustedCentroids.set(idx, adjustedCentroids.get(idx).divide(numOfClusterElements[idx]));
                
                distOfCentr += euclideanDistance(centroids.get(idx), adjustedCentroids.get(idx));   
                
                //centroids is now adjustedCentroids; centroids = new arraylist
                //create empty centroid list at beginning of each loop and fill it in loop
                //next iteration of loop
                centroids.set(idx, adjustedCentroids.get(idx)); // make adjusted centroid the new centroid
                adjustedCentroids.set(idx, SparseVector.zero(vector_length));
            }
            
            if (distOfCentr/centroids.size() < 10) {
                converged = true;
            }
        }
        
        return centroids;
    }
    
}
