package clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private static final int NUM_OF_CLUSTERS = 3;
    private final int vector_length;  // vocabulary size
    private final int num_of_docs;  // document count
    private final Matrix documentVectors;
    
    public KMeans(Matrix documentVectors) {
        this.vector_length = documentVectors.columns();
        this.num_of_docs = documentVectors.rows();
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
    public Map<Vector, List<Vector>> kmeans() {
        Map<Vector, List<Vector>> clusters = new HashMap(); // do NOT remove duplicated -> list
        List<Vector> centroids = new ArrayList<>(); // unique vectors, no duplicates -> set
       
        /*
        //Create random vectors as first centroids
        for(int i = 0; i < NUM_OF_CLUSTERS; i++) {
            //double random = documentVectors.min() + (Math.random() * documentVectors.max());
            //TODO: set lower and upper bound for initilization
            Random val = new Random();
            Vector seed = SparseVector.random(vector_length, val); // length(, density), random
            centroids.add(seed);
        }
        */
        
        // choose centroids from document vectors
        Random rand = new Random();
        while (centroids.size()<NUM_OF_CLUSTERS){
            int randomRow = rand.nextInt(num_of_docs);
            if (!centroids.contains(documentVectors.getRow(randomRow))){
                centroids.add(documentVectors.getRow(randomRow));
            }
        }
        
        boolean converged = false;
        while (!converged) {
            // After first iteration, centroids will have changed
            // -> get rid of old "centroid->vectors" lists
            clusters = new HashMap();
            
            for (int row = 0; row < num_of_docs; row++) {
                double minimum = Math.pow(2, 30); // just some large number
                Vector closestCentroid = SparseVector.zero(vector_length);
                for (Vector centroid : centroids) {
                    double euc = euclideanDistance(documentVectors.getRow(row), centroid);
                    if (euc < minimum) {
                        minimum = euc;
                        closestCentroid = centroid;
                    }
                }
                if (!clusters.containsKey(closestCentroid)){
                    List<Vector> emptyVectorSet = new ArrayList<>();
                    clusters.putIfAbsent(closestCentroid, emptyVectorSet);
                }
                clusters.get(closestCentroid).add(documentVectors.getRow(row));
            }
            
            // distance between old and recomputed centroids
            int distOfCentr = 0;
            
            // Recompute new centroids
            for (Map.Entry<Vector,List<Vector>> cluster : clusters.entrySet()) {
                
                Vector adjustedCentroid = SparseVector.zero(vector_length);
                List<Vector> clusterVectors = cluster.getValue();
                for (Vector vec : clusterVectors) {
                    adjustedCentroid = adjustedCentroid.add(vec);
                }
                adjustedCentroid = adjustedCentroid.divide(clusterVectors.size());
                
                centroids.remove(cluster.getKey());
                centroids.add(adjustedCentroid);
                distOfCentr += cluster.getKey().sum()-adjustedCentroid.sum();
            }    
            
            // centroids do not move much anymore (distance between previous and new centroid is small)
            // Alternative: sum of distances of the instances to the centroids does not change anymore
            if (distOfCentr/centroids.size() < 1) { //TODO: how to decide for a number?
                converged = true;
            }
        }
        
        // convert centroid vector to cluster number ?
        
        return clusters;
    }
    
}
