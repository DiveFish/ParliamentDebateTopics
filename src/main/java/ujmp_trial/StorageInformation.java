package ujmp_trial;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.ujmp.core.doublematrix.SparseDoubleMatrix;
import org.ujmp.core.doublematrix.SparseDoubleMatrix2D;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageInformation implements Serializable {

    private SparseDoubleMatrix2D tfidf; // matrix information
    private SparseDoubleMatrix centroids;
    private BiMap<String, Integer> documentIndices;
    //private Map<String, List<String>> metadata;
    private Map<Integer, Date> dateIds;

    public StorageInformation() {
        tfidf = SparseDoubleMatrix2D.Factory.zeros(0, 0);
        centroids = SparseDoubleMatrix.Factory.zeros(0, 0);
        documentIndices = HashBiMap.create();
        //metadata = new HashMap<>();
        dateIds = new HashMap();
    }

    public StorageInformation(SparseDoubleMatrix2D tfidf, SparseDoubleMatrix centroids, BiMap<String, Integer> documentIndices, Map<Integer, Date> dateIds) {
        this.tfidf = tfidf;
        this.centroids = centroids;
        this.documentIndices = documentIndices;
        //this.metadata = metadata;
        this.dateIds = dateIds;
    }

    public SparseDoubleMatrix2D getTfidf() { return tfidf; }

    public SparseDoubleMatrix getCentroids() {
        return centroids;
    }

    public BiMap<String, Integer> getDocumentIndices() {
        return documentIndices;
    }

    public Map<Integer, Date> getDocumentDates() {
        return dateIds;
    }
}