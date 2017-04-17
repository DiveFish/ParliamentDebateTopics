package compact;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.la4j.Vector;
import org.la4j.iterator.MatrixIterator;
import org.la4j.iterator.VectorIterator;
import org.la4j.matrix.SparseMatrix;
import org.la4j.vector.SparseVector;
import org.ujmp.core.doublematrix.SparseDoubleMatrix2D;

import java.io.Serializable;
import java.util.*;

public class StorageInformation implements Serializable {

    private SparseDoubleMatrix2D serializableCounts; // matrix information
    private SparseDoubleMatrix2D serializableCentroids;
    private BiMap<String, Integer> documentIndices;
    //private Map<String, List<String>> metadata;
    private Map<Integer, Date> dateIds;

    private SparseMatrix counts;
    private List<Vector> centroids;

    public StorageInformation() {
        serializableCounts = SparseDoubleMatrix2D.Factory.zeros(0, 0);
        serializableCentroids = SparseDoubleMatrix2D.Factory.zeros(0, 0);
        documentIndices = HashBiMap.create();
        //metadata = new HashMap<>();
        dateIds = new HashMap();
    }

    public StorageInformation(SparseDoubleMatrix2D tfidf, SparseDoubleMatrix2D serializableCentroids, BiMap<String, Integer> documentIndices, Map<Integer, Date> dateIds) {
        this.serializableCounts = tfidf;
        this.serializableCentroids = serializableCentroids;
        this.documentIndices = documentIndices;
        //this.metadata = metadata;
        this.dateIds = dateIds;
    }


    public void matrixToSerializable(SparseMatrix counts) {
        serializableCounts = SparseDoubleMatrix2D.Factory.zeros(counts.rows(), counts.columns());
        MatrixIterator matIter = counts.nonZeroIterator();
        while (matIter.hasNext()) {
            double val = matIter.next();
            int i = matIter.rowIndex();
            int j = matIter.columnIndex();
            serializableCounts.setAsDouble(val, i, j);
        }
    }

    public void serializableToMatrix() {
        counts = SparseMatrix.zero((int) serializableCounts.getRowCount(), (int) serializableCounts.getColumnCount());
        Iterator<long[]> iter = serializableCounts.nonZeroCoordinates().iterator();
        while (iter.hasNext()) {
            long[] coords = iter.next();
            int row = (int) coords[0];
            int col = (int) coords[1];
            double val = serializableCounts.getAsDouble(coords);
            counts.set(row, col, val);
        }
    }

    public void centroidsToSerializable(List<Vector> centroids) {
        serializableCentroids = SparseDoubleMatrix2D.Factory.zeros(centroids.size(), serializableCounts.getColumnCount());
        int row = 0;
        for (Vector vec : centroids) {
            VectorIterator matIter = vec.iterator();
            while (matIter.hasNext()) {
                int col = matIter.index();
                double val = matIter.next();
                serializableCentroids.setAsDouble(val, row, col);
            }
            row++;
        }
    }

    public void serializableToCentroids() {
        centroids = new ArrayList<>();
        int rowSize = (int) serializableCentroids.getColumnCount();
        int newRow = 0;
        SparseVector vec = SparseVector.zero(rowSize);
        Iterator<long[]> iter = serializableCentroids.nonZeroCoordinates().iterator();
        while (iter.hasNext()) {
            long[] coords = iter.next();
            int row = (int) coords[0];
            int col = (int) coords[1]+1;  //TODO: WHY column count by 1 too low >:(

            // new row -> add vector of PREVIOUS row to vector list
            if (row == newRow+1) {
                centroids.add(vec);
                newRow = row;
                vec = SparseVector.zero(rowSize);
            }
            // in case of zero-rows (difference between row and newRow > 1) -> add empty vector
            else if (row > newRow) {
                while (row > newRow) {
                    centroids.add(SparseVector.zero(rowSize));
                    newRow++;
                }
                vec = SparseVector.zero(rowSize);
            }

            vec.set(col, serializableCentroids.getAsDouble(coords));

            // at last non-zero coordinate -> add LAST vector to vector list
            if (!iter.hasNext()) {
                centroids.add(vec);
            }
        }
    }

    public SparseMatrix getCountsMatrix() { return counts; }

    public List<Vector> getCentroids() {
        return centroids;
    }

    public BiMap<String, Integer> getDocumentIndices() {
        return documentIndices;
    }

    public Map<Integer, Date> getDocumentDates() {
        return dateIds;
    }
}