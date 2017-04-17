package compact;

import com.google.common.collect.BiMap;
import com.google.common.collect.Iterables;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.*;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.util.Pair;
import org.la4j.matrix.SparseMatrix;
import org.la4j.matrix.sparse.CRSMatrix;
import org.la4j.iterator.MatrixIterator;
import org.la4j.Vector;
import org.la4j.decomposition.SingularValueDecompositor;
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

    private static SparseMatrix counts;

    public TermDocumentMatrix(BiMap<String, Integer> documentIndices,
                              BiMap<String, Integer> tokenIndices, Set<Integer> mostFrequent) {
        this.documentIndices = documentIndices;
        this.tokenIndices = tokenIndices;
        counts = CRSMatrix.zero(documentIndices.size(), tokenIndices.size());
    }

    /**
     * @return
     */
    public SparseMatrix counts() {
        return counts;
    }

    /**
     * @param documentFrequencies
     */
    public void tfIdf(TIntList documentFrequencies) {
        countsToTfIdf(documentFrequencies);
    }
    
    /**
     * Decompose matrix.
     */
    public void svd() {
        SingularValueDecompositor svd = new SingularValueDecompositor(counts);
        counts = svd.decompose()[0].toSparseMatrix();
    }

    /**
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
            counts.set(fileIDIndex, tokenID, counts.get(fileIDIndex, tokenID) + entry.getValue());
        }
    }

    /**
     * Turn the raw frequency matrix into a tf.idf matrix.
     *
     * @param documentFrequencies The document frequencies of all terms
     */
    private void countsToTfIdf(TIntList documentFrequencies) {

        int numOfDocuments = counts.rows();

        MatrixIterator matIter = counts.nonZeroIterator();
        while (matIter.hasNext()) {
            double val = matIter.next();
            int j = matIter.columnIndex();
            int docFreq = documentFrequencies.get(j);
            if (docFreq > 0 && val > 0) {
                matIter.set(val * Math.log((double) numOfDocuments / (double) docFreq));
            }
            if (j == counts.columns() - 1) {
                System.out.println(matIter.rowIndex() + 1 + " sections done");
            }
        }
    }

    /**
     * Get n highest tf.idf values from document.
     *
     * @param document The document vector
     * @param n        The number of most relevant terms
     * @param retain   Terms to retain
     * @return The term indices
     */
    private TIntList nHighestTfIdfs(Vector document, Integer n, TIntSet retain) {
        SortedSet<Pair<Integer, Double>> sortedTerms = new TreeSet<>((o1, o2) -> {
            int cmp = o1.getValue().compareTo(o2.getValue());
            if (cmp == 0) {
                return o1.getKey().compareTo(o2.getKey());
            }

            return -cmp;
        });

        VectorIterator vIter = (document.toSparseVector()).nonZeroIterator();
        while (vIter.hasNext()) {
            double tfidf = vIter.next();
            if (retain.contains(vIter.index())) {
                sortedTerms.add(new Pair<>(vIter.index(), tfidf));
            }
        }

        TIntList highestN = new TIntArrayList();
        for (Pair<Integer, Double> pair : Iterables.limit(sortedTerms, n)) {
            highestN.add(pair.getKey());
        }

        return highestN;
    }

    /**
     * Convert indices returned by nHighestTfIdfs to words.
     *
     * @param document     The document vector
     * @param n            The number of most relevant terms
     * @param tokenIndices The indices assigned to the vocabulary
     * @param sharedTerms Token indices of terms shared by docs in a cluster
     * @return The most relevant terms
     */
    public List<String> nMostRelevantTerms(SparseVector document, Integer n, BiMap<String, Integer> tokenIndices, TIntSet sharedTerms) {
        TIntList idxs = nHighestTfIdfs(document, n, sharedTerms);
        List<String> terms = new ArrayList<>();

        for (int i = 0; i < idxs.size(); i++) {
            terms.add(tokenIndices.inverse().get(idxs.get(i)));
        }

        System.out.println(terms);

        return terms;
    }

    /**
     * Collect all words which are shared between documents from a cluster. Take
     * all words from first document ("if") and remove those which are not
     * contained in the other documents ("else").
     *
     * @param cluster The row idxs of the vectors
     * @return Token indices of words shared by ALL documents
     */
    public TIntSet sharedTerms(TIntList cluster) {
        TIntSet shared = null;

        for (int i = 0; i < cluster.size(); i++) {
            SparseVector row = counts.getRow(cluster.get(i)).toSparseVector();
            TIntSet docTermSet = new TIntHashSet();
            VectorIterator termIter = row.nonZeroIterator();
            while (termIter.hasNext()) {
                termIter.next();
                docTermSet.add(termIter.index());
            }

            if (i == 0) {
                shared = docTermSet;
            } else {
                shared.retainAll(docTermSet);
            }
        }

        return shared;
    }
    
    public TIntSet partiallySharedTerms(TIntList cluster,double ratio) {
        TIntSet partShared = new TIntHashSet();        
        TIntList terms = new TIntArrayList();
        TIntList freqs = new TIntArrayList();
        
        for (int i = 0; i < cluster.size(); i++) {
            SparseVector row = counts.getRow(cluster.get(i)).toSparseVector();
            VectorIterator termIter = row.nonZeroIterator();
            while (termIter.hasNext()) {
                termIter.next();
                int termId = termIter.index();
                int idx = terms.binarySearch(termId);
                if (idx < 0) {
                    terms.add(termId);
                    freqs.add(1);
                }
                else {
                    freqs.set(idx, freqs.get(idx)+1);
                }
            }
        }
        
        for (int j = 0; j < terms.size(); j++) {
            if (freqs.get(j) >= cluster.size()*ratio) {
                partShared.add(terms.get(j));
            }
        }
        
        return partShared;
    }
}
