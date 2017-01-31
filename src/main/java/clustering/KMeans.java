package clustering;

import com.google.common.collect.BiMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class KMeans {
    private final int numOfClusters;
    private final Matrix documentVectors;
    
    public KMeans(int numOfClusters, Matrix documentVectors) {
        this.numOfClusters = numOfClusters;
        this.documentVectors = documentVectors;
    }
    
    /**
     * Cluster data by k-means clustering. Calculate euclidean distance from all
     * document vectors to all centroids, assign each to their closest centroid.
     * When all documents are assigned to a centroid, readjust centroids.
     *
     * @return The clusters
     * @throws java.io.IOException
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
    //public List<List<Vector>> clusters() {
    public List<TIntList> clusters() throws IOException {    
        if (documentVectors.max() == 0) {
            throw new IOException(String.format("Trying to extract clusters from zero matrix"));
        }
        
        int vector_length = documentVectors.columns();  //vocab size
        int num_of_docs = documentVectors.rows();
        
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
        
        // Choose random centroids from document vectors
        List<Vector> centroids = new ArrayList<>();
        Random rand = new Random();
        while (centroids.size() < numOfClusters) {
            int randomRow = rand.nextInt(num_of_docs);
            System.out.println(documentVectors.getRow(randomRow));
            if (!centroids.contains(documentVectors.getRow(randomRow))) {
                centroids.add(documentVectors.getRow(randomRow));
                System.out.println("added");
            }
        }
        
        Map<Vector, List<Vector>> clusters = new HashMap();
        Map<Vector, TIntList> docClusters = new HashMap();
        boolean converged = false;
        while (!converged) {
            // After first iteration, centroids will have changed -> get rid of old "centroid->vectors" lists
            clusters = new HashMap();
            docClusters = new HashMap();
            
            // Assign vectors to their closest centroid
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
                if (!clusters.containsKey(closestCentroid)) {
                    List<Vector> emptyVectorSet = new ArrayList<>();
                    clusters.putIfAbsent(closestCentroid, emptyVectorSet);
                    
                    TIntList emptyDocSet = new TIntArrayList();
                    docClusters.putIfAbsent(closestCentroid, emptyDocSet);
                }
                clusters.get(closestCentroid).add(documentVectors.getRow(row));
                docClusters.get(closestCentroid).add(row);
            }
            
            // Distance between old and recomputed centroids
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
            
            // Centroids do not move much anymore (distance between previous and new centroid is small)
            // Alternative: sum of distances of the instances to the centroids does not change anymore
            if (distOfCentr/centroids.size() < 1) { //TODO: how to decide for a number?
                converged = true;
            }
        }
        
        // Convert centroid vectors to list of clusters where index of cluster can be used as the cluster number
        List<List<Vector>> clustersByNumber = new ArrayList();
        for (Map.Entry<Vector,List<Vector>> cluster : clusters.entrySet()) {
            clustersByNumber.add(cluster.getValue());
        }
        
        List<TIntList> docClustersByNumber = new ArrayList();
        for (Map.Entry<Vector,TIntList> docCluster : docClusters.entrySet()) {
            docClustersByNumber.add(docCluster.getValue());
        }
        
        return docClustersByNumber;
    }
    
}
