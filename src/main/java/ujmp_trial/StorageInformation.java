package ujmp_trial;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.ujmp.core.doublematrix.SparseDoubleMatrix;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageInformation implements Serializable {

    private SparseDoubleMatrix centroids;
    private BiMap<String, Integer> documentIndices;
    private Map<String, List<String>> metadata;

    public StorageInformation() {
        centroids = SparseDoubleMatrix.Factory.zeros(0, 0);
        documentIndices = HashBiMap.create();
        metadata = new HashMap<>();
    }

    public StorageInformation(SparseDoubleMatrix centroids, BiMap<String, Integer> documentIndices, Map<String, List<String>> metadata) {
        this.centroids = centroids;
        this.documentIndices = documentIndices;
        this.metadata = metadata;
    }

    public SparseDoubleMatrix getCentroids() {
        return centroids;
    }

    public BiMap<String, Integer> getDocumentIndices() {
        return documentIndices;
    }

    public Map<String, List<String>> getMetadata() {
        return metadata;
    }
}