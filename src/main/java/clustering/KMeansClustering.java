package clustering;

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
public class KMeansClustering {
    private final int numOfClusters;
    private final Matrix documentVectors;
    
    public KMeansClustering(int numOfClusters, Matrix documentVectors) {
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
    
    public List<Vector> centroids() throws IOException {    
        
        if (documentVectors.max() == 0) {
            throw new IOException(String.format("Trying to extract clusters from zero matrix"));
        }
        
        int vector_length = documentVectors.columns();  //vocab size
        int num_of_docs = documentVectors.rows();
        
        List<Vector> centroids = new ArrayList<>();
        
        // Choose random centroids from document vectors
        Random rand = new Random(28); //TODO: give as argument to method, set seed
        while (centroids.size() < numOfClusters) {
            int randomRow = rand.nextInt(num_of_docs);
            if (!centroids.contains(documentVectors.getRow(randomRow))) {
                centroids.add(documentVectors.getRow(randomRow));
            }
        }
        
        List<List<Vector>> clusters = new ArrayList<>(centroids.size());  // the clusters as vectors
        clusters.addAll(new ArrayList());
        
        boolean converged = false;
        while (!converged) {
            // Assign vectors to their closest centroid
            for (int row = 0; row < num_of_docs; row++) {
                double minimum = Double.MAX_VALUE;
                Vector closestCentroid = SparseVector.zero(vector_length);
                
                for (Vector centroid : centroids) {
                    double euc = euclideanDistance(documentVectors.getRow(row), centroid);
                    if (euc < minimum) {
                        minimum = euc;
                        closestCentroid = centroid;
                    }
                }
                clusters.get(centroids.indexOf(closestCentroid)).add(documentVectors.getRow(row));
            }
            
            // Distance between old and recomputed centroids
            int distOfCentr = 0;
            // Recompute new centroids
            for (List<Vector> cluster : clusters) {
                Vector adjustedCentroid = SparseVector.zero(vector_length);
                for (Vector vec : cluster) {
                    adjustedCentroid = adjustedCentroid.add(vec);
                }
                adjustedCentroid = adjustedCentroid.divide(cluster.size());
                //TODO: distance between old and new centroid
                // vec1+(-vec2) -> l2 norm
                centroids.set(clusters.indexOf(cluster), adjustedCentroid);
            }    
            if (distOfCentr/centroids.size() < 10) {
                converged = true;
            }
        }
        
        return centroids;
    }
    
}
