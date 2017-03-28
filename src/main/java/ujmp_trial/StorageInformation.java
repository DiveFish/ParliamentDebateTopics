package ujmp_trial;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.ujmp.core.doublematrix.SparseDoubleMatrix;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageInformation implements Serializable {

    private SparseDoubleMatrix centroids;
    private BiMap<String, Integer> documentIndices;
    //private Map<String, List<String>> metadata;
    private Map<Integer, Date> dateIds;

    public StorageInformation() {
        centroids = SparseDoubleMatrix.Factory.zeros(0, 0);
        documentIndices = HashBiMap.create();
        //metadata = new HashMap<>();
        dateIds = new HashMap();
    }

    public StorageInformation(SparseDoubleMatrix centroids, BiMap<String, Integer> documentIndices, Map<Integer, Date> dateIds) {
        this.centroids = centroids;
        this.documentIndices = documentIndices;
        //this.metadata = metadata;
        this.dateIds = dateIds;
    }

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