package clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import static matrix.MatrixMeasures.euclideanDistance;
import org.la4j.Vector;
import org.la4j.Matrix;
import org.la4j.vector.SparseVector;

/**
 * K-means clustering for document vectors.
 *
 * @author Patricia Fischer
 */
public class KMeans {
    private static final int NUM_OF_CLUSTERS = 15;
    private final int vector_length;  // vocabulary size
    private final Matrix documentVectors;
    
    public KMeans(Matrix documentVectors) {
        this.vector_length = documentVectors.columns();
        this.documentVectors = documentVectors;
    }
    
    /**
     * Cluster data by k-means clustering. Calculate euclidean distance from all
     * document vectors to all centroids, assign each to their closest centroid.
     * When all documents are assigned to a centroid, readjust centroids.
     *
     * @return The clusters
     */
    
    /*
    Pseudocode for K-Means
    - initialize random centroid vectors
    - while (stopping criterion)
    -- assign empty set to all clusters
    -- assign all document vectors to their closest centroid
    -- add all vectors to the respective cluster
    -- recompute centroids
    */
    public Map<Vector, Set<Vector>> kmeans() {
        Map<Vector, Set<Vector>> clusters = new HashMap();
        Set<Vector> centroids = new HashSet<>();
       
        //Create random vectors as first centroids
        for(int i = 0; i < NUM_OF_CLUSTERS; i++) {
            //double random = documentVectors.min() + (Math.random() * documentVectors.max());
            
            //TODO: set lower and upper bound for initilization
            Random val = new Random();
            Vector seed = SparseVector.random(vector_length, val); // length(, density), random
            centroids.add(seed);
        }
        
        boolean stop = false;
        while (!stop) {
            clusters.clear();  // centroids have changed, therefore get rid of old "centroid->vectors" lists
            
            for (int row = 0; row < documentVectors.rows(); row++) {
                double minimum = Math.pow(2, 30); // just some large number
                Vector closestCentroid = SparseVector.zero(vector_length);
                for (Vector centroid : centroids) {
                    double euc = euclideanDistance(documentVectors.getRow(row), centroid);
                    if (euc < minimum) {
                        minimum = euc;
                        closestCentroid = centroid;
                    }
                }
                clusters.get(closestCentroid).add(documentVectors.getRow(row));
            }
            
            // Recompute new centroids
            for (Map.Entry<Vector,Set<Vector>> cluster : clusters.entrySet()) {
                
                Vector adjustedCentroid = SparseVector.zero(vector_length);
                Set<Vector> clusterVectors = cluster.getValue();
                for (Vector vec : clusterVectors) {
                    adjustedCentroid.add(vec);
                }
                adjustedCentroid.divide(clusterVectors.size());
                
                centroids.remove(cluster.getKey());
                centroids.add(adjustedCentroid);
            }    
            
            if (stop) {
                stop = true;
                /*
                - centroids do not move much anymore (distance between previous and new centroid is small)
                - instances do not change the cluster anymore
                - sum of distances of the instances to the centroids does not change anymore
                */
            }
        }
        
        // convert centroid vector to cluster number ?
        
        return clusters;
    }
    
}
