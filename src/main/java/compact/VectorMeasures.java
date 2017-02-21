package compact;

import org.la4j.Vector;

/**
 * Apply basic measures on vectors retrieved from a matrix.
 *
 * @author Patricia Fischer
 */
public class VectorMeasures {
    
    /**
     * Calculate the cosine similarity between two vectors. The dot product is
     * divided by the length of the vectors. The norm() method on a Vector
     * returns the euclidean norm of the vector.
     * 
     * @param v1 The first vector
     * @param v2 The second vector
     * @return The cosine similarity between v1 and v2
     */
    public static double cosineSimilarity(Vector v1, Vector v2) {
        double dp = 0;
        // TODO: use iterator here
        for (int i = 0; i < v1.length(); i++) {
            dp += v1.get(i) * v2.get(i);
        }
        
        return dp / (v1.norm() * v2.norm());
    }
    
    /**
     * Calculate the euclidean distance between two vectors. Normalize vectors
     * before getting the euclidean distance between them.
     * 
     * @param v1 The first vector
     * @param v2 The second vector
     * @return The euclidean distance between v1 and v2
     */
    public static double euclideanDistance(Vector v1, Vector v2) {
        
        v1 = v1.divide(v1.norm());
        v2 = v2.divide(v2.norm());
        
        return v1.subtract(v2).norm(); 
    }
}
