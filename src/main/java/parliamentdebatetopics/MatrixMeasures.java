package parliamentdebatetopics;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.la4j.Vector;
import org.la4j.vector.SparseVector;

/**
 * Apply basic measures on vectors retrieved from a matrix.
 *
 * @author Patricia Fischer
 */
public class MatrixMeasures {
    
    /* //To check functionality of MatrixMeasures
    private final static Vector v1 = SparseVector.zero(4);
    private final static Vector v2 = SparseVector.zero(4);
    
    public static void main(String[] args) {
        v1.set(0,2);
        v1.set(1,2);
        v1.set(2,2);
        v1.set(3,2);
        v2.set(0,1);
        v2.set(1,1);
        v2.set(2,1);
        v2.set(3,1);
        
        System.out.println(cosineSimilarity(v1,v2));
        System.out.println(euclideanDistance(v1,v2));
    }
    */

    /**
     * Calculate the cosine similarity between two vectors. The dot product is
     * divided by the length of the vectors. The norm() method on a Vector
     * returns the euclidean norm of the vector.
     * @param v1 The first vector
     * @param v2 The second vector
     * @return The cosine similarity between v1 and v2
     */
    public static double cosineSimilarity(Vector v1, Vector v2){
        MutableDouble dp = new MutableDouble();
        for (int i = 0; i < v1.length(); i++) {
            dp.add(v1.get(i) * v2.get(i));
        }
        
        return dp.doubleValue() / (v1.norm() * v2.norm());
    }
    
    /**
     * Calculate the euclidean distance between two vectors.
     * @param v1 The first vector
     * @param v2 The second vector
     * @return The euclidean distance between v1 and v2
     */
    public static double euclideanDistance(Vector v1, Vector v2){
        MutableDouble euc = new MutableDouble();
        for (int i = 0; i < v1.length(); i++){
            euc.add(Math.pow(v2.get(i) - v1.get(i),2));
        } 
        
        return Math.sqrt(euc.doubleValue());
    }
}
