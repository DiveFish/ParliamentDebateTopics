package matrix;

import org.la4j.Vector;
import org.la4j.iterator.VectorIterator;

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
        for (int i = 0; i < v1.length(); i++) {
            dp += v1.get(i) * v2.get(i);
        }
        
        return dp / (v1.norm() * v2.norm());
    }
    
    /**
     * Calculate the euclidean distance between two vectors.
     * 
     * @param v1 The first vector
     * @param v2 The second vector
     * @return The euclidean distance between v1 and v2
     */
    public static double euclideanDistance(Vector v1, Vector v2) {
        
        double euc = 0;
        Vector v = v1.subtract(v2);
        
        VectorIterator vIter = v.iterator();
        while (vIter.hasNext()) {
            double val = vIter.next();
            euc += Math.pow(val, 2);
        }
        
        return Math.sqrt(euc);
    }
    
    //REJECTED METHODS FOR EUCLIDEAN DISTANCE
        /* Slower than iterator: for-loop
        for (int i = 0; i < v.length(); i++) {
            euc += Math.pow(v.get(i), 2);
        }
        return Math.sqrt(euc);
        */
        
        /*
        Accessing both vectors and doing the calculations is slower
        for (int i = 0; i < v1.length(); i++) {
            euc += Math.pow(v2.get(i) - v1.get(i), 2);
        }
        */
        
        // Alternative l2norm(vec1+(-vec2)) is also slower
        //return v1.add(v2.multiply(-1)).norm();
    
}
