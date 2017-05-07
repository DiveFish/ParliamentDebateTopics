package compact;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.la4j.Vector;
import org.la4j.iterator.MatrixIterator;
import org.la4j.iterator.VectorIterator;
import org.la4j.matrix.SparseMatrix;
import org.la4j.vector.SparseVector;

import java.io.Serializable;
import java.util.*;

/**
 * @author Patricia Fischer
 */
public class StorageInformation implements Serializable {

    private BiMap<String, Integer> documentIndices;
    private BiMap<String, Integer> tokenIndices;
    private Map<Integer, Date> dateIds;

    // Containers to serialize non-serializable matrix and centroid objects
    private List<MatrixValue> serializableCounts;
    // size of matrix:
    private int numOfRows;
    private int numOfCols;
    private List<List<MatrixValue>> serializableCentroids;

    // SparseMatrix and Vector from the la4j package are non-serializable objects
    // -> fill with values from serialized list objects
    private SparseMatrix counts;
    private List<Vector> centroids;

    public StorageInformation() {
        serializableCounts = new ArrayList<>();
        numOfRows = 0;
        numOfCols = 0;
        serializableCentroids = new ArrayList<>();
        documentIndices = HashBiMap.create();
        tokenIndices = HashBiMap.create();
        dateIds = new HashMap();
    }

    public StorageInformation(List<MatrixValue> serializableCounts, int numOfRows, int numOfCols, List<List<MatrixValue>> serializableCentroids, BiMap<String, Integer> documentIndices,BiMap<String, Integer> tokenIndices, Map<Integer, Date> dateIds) {
        this.serializableCounts = serializableCounts;
        this.numOfRows = numOfRows;
        this.numOfCols = numOfCols;
        this.serializableCentroids = serializableCentroids;
        this.documentIndices = documentIndices;
        this.tokenIndices = tokenIndices;
        this.dateIds = dateIds;
    }

    public void matrixToSerializable(SparseMatrix counts) {
        MatrixIterator matIter = counts.nonZeroIterator();
        while (matIter.hasNext()) {
            double val = matIter.next();
            int i = matIter.rowIndex();
            int j = matIter.columnIndex();
            serializableCounts.add(new MatrixValue(i, j, val));
        }
    }

    public void serializableToMatrix() {
        counts = SparseMatrix.zero(numOfRows, numOfCols);
        for (MatrixValue c : serializableCounts) {
            counts.set(c.getRow(), c.getColumn(), c.getValue());
        }
    }

    public void centroidsToSerializable(List<Vector> centroidList) {
        for (Vector centr : centroidList) {
            List<MatrixValue> centrList = new ArrayList<>();
            VectorIterator vIter = centr.toSparseVector().nonZeroIterator();
            while (vIter.hasNext()) {
                double val = vIter.next();
                int i = vIter.index();
                centrList.add(new MatrixValue(0, i, val)); // treat vector like a one-dimensionsional matrix
            }
            serializableCentroids.add(centrList);
        }
    }

    public void serializableToCentroids() {
        centroids = new ArrayList<>();
        for (List<MatrixValue> vals : serializableCentroids) {
            Vector centr = SparseVector.zero(numOfCols);
            for (MatrixValue val : vals) {
                centr.set(val.getColumn(), val.getValue());
            }
            centroids.add(centr);
        }
    }

    public SparseMatrix getCountsMatrix() { return counts; }

    public List<Vector> getCentroids() {
        return centroids;
    }

    public BiMap<String, Integer> getDocumentIndices() {
        return documentIndices;
    }

    public BiMap<String, Integer> getTokenIndices() {
        return tokenIndices;
    }

    public Map<Integer, Date> getDocumentDates() {
        return dateIds;
    }
}