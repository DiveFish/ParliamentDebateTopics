package ujmp_trial;

import com.google.common.collect.BiMap;
import com.google.common.collect.Iterables;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.*;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.util.Pair;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.doublematrix.SparseDoubleMatrix2D;

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

    private static SparseDoubleMatrix2D counts;

    public TermDocumentMatrix(BiMap<String, Integer> documentIndices,
                              BiMap<String, Integer> tokenIndices, Set<Integer> mostFrequent) {
        this.documentIndices = documentIndices;
        this.tokenIndices = tokenIndices;
        //counts = new SparseDoubleMatrix2D(documentIndices.size(), tokenIndices.size());
        counts = SparseDoubleMatrix2D.Factory.zeros(documentIndices.size(), tokenIndices.size());
    }

    
    /**
     * Not used because of heap space exception.
     */
    public void svd() {
        counts = (SparseDoubleMatrix2D) counts.svd()[0];
    }
    
    /**
     * @return The term-frequency matrix
     */
    public SparseDoubleMatrix2D counts() {
        return counts;
    }

    /**
     * Transform the frequency into tf.idf matrix.
     *
     * @param documentFrequencies
     */
    public void tfIdf(TIntList documentFrequencies) {
        countsToTfIdf(documentFrequencies);
    }

    private void countsToTfIdf(TIntList documentFrequencies) {
        int numOfDocuments = (int) counts.getRowCount();
        for (long[] l: counts.nonZeroCoordinates()) {
            double val = counts.getAsDouble(l);
            int row = (int) l[0];
            int col = (int) l[1];
            int docFreq = documentFrequencies.get(col);
            if (docFreq > 0 && val > 0) {
                counts.setAsDouble( val * Math.log((double) numOfDocuments / (double) docFreq), row, col);
            }
        }
        // Very slow too :(
        //counts = (SparseDoubleMatrix2D) counts.normalize(Calculation.Ret.NEW, 1).toDoubleMatrix();
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
            counts.setAsDouble(counts.getAsDouble(fileIDIndex, tokenID) + entry.getValue(), fileIDIndex, tokenID);
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
    private TIntList nHighestTfIdfs(SparseDoubleMatrix2D document, Integer n, TIntSet retain) {
        SortedSet<Pair<Integer, Double>> sortedTerms = new TreeSet<>((o1, o2) -> {
            int cmp = o1.getValue().compareTo(o2.getValue());
            if (cmp == 0) {
                return o1.getKey().compareTo(o2.getKey());
            }

            return -cmp;
        });

        for (int i = 0; i < document.getColumnCount(); i++) {
            double tfidf = document.getAsDouble(0, i);
            if (retain.contains(i)) {
                sortedTerms.add(new Pair<>(i, tfidf));
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
    public List<String> nMostRelevantTerms(SparseDoubleMatrix2D document, Integer n, BiMap<String, Integer> tokenIndices, TIntSet sharedTerms) {
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
            Iterator<long[]> rowIter = counts.selectRows(Calculation.Ret.NEW, cluster.get(i)).nonZeroCoordinates().iterator();
            TIntSet docTermSet = new TIntHashSet();
            while(rowIter.hasNext()) {
                docTermSet.add((int) rowIter.next()[1]);
            }

            if (i == 0) {
                shared = docTermSet;
            } else {
                shared.retainAll(docTermSet);
            }
        }

        return shared;
    }

    /**
     *
     * @param cluster
     * @param ratio
     * @return
     */
    public TIntSet partiallySharedTerms(TIntList cluster, double ratio) {
        TIntSet partShared = new TIntHashSet();
        TIntList terms = new TIntArrayList(); //list of terms occurring in this cluster
        TIntList freqs = new TIntArrayList(); //the terms' frequencies (used as filter)

        for (int i = 0; i < cluster.size(); i++) {
            Iterator<long[]> rowIter = counts.selectRows(Calculation.Ret.NEW, cluster.get(i)).nonZeroCoordinates().iterator();
            while (rowIter.hasNext()) {
                long[] coords = rowIter.next();
                int termId = (int) coords[1];
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
            if (freqs.get(j) > cluster.size()*ratio) {
                partShared.add(terms.get(j));
            }
        }

        return partShared;
    }
}
