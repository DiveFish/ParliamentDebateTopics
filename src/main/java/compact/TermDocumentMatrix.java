package compact;

import com.google.common.collect.BiMap;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.la4j.matrix.SparseMatrix;
import org.la4j.matrix.sparse.CRSMatrix;
import org.la4j.iterator.MatrixIterator;
import org.la4j.Vector;
import org.la4j.iterator.VectorIterator;
import org.la4j.vector.SparseVector;

/**
 * Create a term-document and tf.idf matrix. All files and the vocabulary
 * extracted from the them are associated with indices that can be used to
 * construct the term-document matrix, the rows referring to the document
 * indices and the columns to the token indices.
 * 
 * @author Daniël de Kok and Patricia Fischer
 */
public class TermDocumentMatrix {

    private final BiMap<String, Integer> documentIndices;
    
    private final BiMap<String, Integer> tokenIndices;
    
    private final Set<Integer> mostFrequent;
    
    private static SparseMatrix counts;
    
    public TermDocumentMatrix(BiMap<String, Integer> documentIndices,
            BiMap<String, Integer> tokenIndices, Set<Integer> mostFrequent) {
        this.documentIndices = documentIndices;
        this.tokenIndices = tokenIndices;
        this.mostFrequent = mostFrequent;
        counts = CRSMatrix.zero(documentIndices.size(), tokenIndices.size());
    }
    
    /**
     *
     * @return
     */
    public SparseMatrix counts() {
        return counts;
    }
    
    /**
     *
     * @param documentFrequencies
     */
    public void tfIdf(List<TIntList> documentFrequencies) {
        countsToTfIdf(documentFrequencies);
    }
    
    /**
     *
     * @param sectionID
     * @param wordFrequencies
     * @throws IOException
     */
    public void processSection(String sectionID, Map<String, Integer> wordFrequencies) throws IOException {
        
        Integer fileIDIndex = documentIndices.get(sectionID);
        if (fileIDIndex == null) {
            throw new IOException(String.format("Unknown file ID: %s", sectionID));
        }
        
        for (Map.Entry<String, Integer> entry : wordFrequencies.entrySet()) {
            Integer tokenID = tokenIndices.get(entry.getKey());
            if (!mostFrequent.contains(tokenID)) {
                counts.set(fileIDIndex, tokenID, counts.get(fileIDIndex, tokenID)+entry.getValue());
            }
        }   
    }
    
    /**
     * Turn the raw frequency matrix into a tf.idf matrix.
     * 
     * @param documentFrequencies The document frequencies of all terms
     */
    private void countsToTfIdf(List<TIntList> documentFrequencies) {
        
        int numOfDocuments = counts.rows();
        
        MatrixIterator matIter = counts.nonZeroIterator();
        while (matIter.hasNext()) {
            double val = matIter.next();
            int j = matIter.columnIndex();
            int docFreq = documentFrequencies.get(j).size();
            if (docFreq > 0 && val > 0) {
                matIter.set(val * Math.log(numOfDocuments/docFreq));
            }
            if (j == counts.columns()-1) {
                System.out.println(matIter.rowIndex()+1+" sections done");
            }
        }
    }
    
    /**
     * Get n highest tf.idf values from document.
     *
     * @param document The document vector
     * @param n The number of most relevant terms
     * @return The term indices
     */
    private TIntList nHighestTfIdfs(Vector document, Integer n) {
        TDoubleList tfIdfs = new TDoubleArrayList(n);
        tfIdfs.fill(0, n, 0);
        TIntList idxs = new TIntArrayList(n);
        idxs.fill(0, n, 0);
        VectorIterator vIter = document.iterator();
        
        while (vIter.hasNext()) {
            int mid = 0;
            double val = vIter.next();
            if (val > tfIdfs.get(0)) {
                int lo = 0;
                int hi = tfIdfs.size() - 1;
                while (lo <= hi) {
                    mid = lo + (hi - lo) / 2;
                    if (val < tfIdfs.get(mid)) {
                        hi = mid - 1;
                    }
                    else if (val > tfIdfs.get(mid)) {
                        lo = mid + 1;
                    }
                    else
                        break;
                }
                tfIdfs.insert(mid, val);  // tf.idf value
                tfIdfs.removeAt(0);
                idxs.insert(mid, vIter.index());  // index of tf.idf value in vector -> to get word from tokenIndices
                idxs.removeAt(0);
            }
        }
        return idxs.subList(0, idxs.size()-1);
    }
    
    /**
     * Convert indices returned by nHighestTfIdfs to words.
     *
     * @param document The document vector
     * @param n The number of most relevant terms
     * @param tokenIndices The indices assigned to the vocabulary
     * @return The most relevant terms
     */
    public Set<String> nMostRelevantTerms(SparseVector document, Integer n, BiMap<String, Integer> tokenIndices) {
        TIntList idxs = nHighestTfIdfs(document, n);
        Set<String> terms = new HashSet();
        
        for (int i = 0; i < idxs.size(); i++) {
            terms.add(tokenIndices.inverse().get(idxs.get(i)));
        }
        System.out.println(terms);
        return terms;
    }
}
