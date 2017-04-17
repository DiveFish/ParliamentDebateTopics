package ujmp_trial;

import com.google.common.collect.BiMap;
import com.google.common.collect.Iterables;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.*;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.util.Pair;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.doublematrix.SparseDoubleMatrix;
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

    private TDoubleList rowSizes;

    public TermDocumentMatrix(BiMap<String, Integer> documentIndices,
                              BiMap<String, Integer> tokenIndices, Set<Integer> mostFrequent) {
        this.documentIndices = documentIndices;
        this.tokenIndices = tokenIndices;
        //counts = new SparseDoubleMatrix2D(documentIndices.size(), tokenIndices.size());
        counts = SparseDoubleMatrix2D.Factory.zeros(documentIndices.size(), tokenIndices.size());
        rowSizes = new TDoubleArrayList();
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

    public TDoubleList vectorSizes() { return rowSizes; }

    /**
     * Transform the frequency into tf.idf matrix.
     *
     * @param documentFrequencies
     */
    public void tfIdf(TIntList documentFrequencies) {
        countsToTfIdf(documentFrequencies);
        normalizeCountsMatrix();
    }

    /**
     * Calculate the term-frequency - inverse-document-frequency
     * values for the raw frequency matrix.
     * Save the overall row vector sizes for normalizing.
     *
     * @param documentFrequencies The terms' document frequencies
     */

    private void countsToTfIdf(TIntList documentFrequencies) {
        int numOfDocuments = (int) counts.getRowCount();
        int rowCount = 0;
        double rowVecSize = 0;

        Iterator<long[]> iter = counts.nonZeroCoordinates().iterator();
        while (iter.hasNext()) {
            long[] coords = iter.next();
            int row = (int) coords[0];
            int col = (int) coords[1];

            // 1. Get size of row vector and reset it for NEXT row
            // row > rowCount: We are now in the next row -> completed calculation of previous row's rowSize
            if (row > rowCount && iter.hasNext()) {
                rowSizes.add(Math.sqrt(rowVecSize * rowVecSize));
                rowCount++;
                rowVecSize = 0;
            }

            double val = counts.getAsDouble(coords);
            int docFreq = documentFrequencies.get(col);
            double entry = 0;
            if (docFreq > 0 && val > 0) {
                entry =  val * Math.log((double) numOfDocuments / (double) docFreq);
                counts.setAsDouble(entry , row, col);
                rowVecSize += entry;
            }

            // 2. Get size of LAST row vector
            if (!iter.hasNext()) {
                rowSizes.add(Math.sqrt(rowVecSize * rowVecSize));
            }
        }

        // Very slow too :(
        //counts = (SparseDoubleMatrix2D) counts.normalize(Calculation.Ret.NEW, 1).toDoubleMatrix();
    }

/*
    // CountsToTfIdf without vector length tracker
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
    }*/

    /**
     * Normalize the row vectors by dividing each
     * element by the row vector size.
     */
    private void normalizeCountsMatrix(){
        for (long[] l: counts.nonZeroCoordinates()) {
            int row = (int) l[0];
            int col = (int) l[1];
            counts.setAsDouble(counts.getAsDouble(l) / rowSizes.get(row) , row, col);
        }
    }

    /**
     * @param sectionID
     * @param wordFrequencies
     * @throws IOException
     */
    public void processSection(String sectionID, Map<String, Integer> wordFrequencies) throws IOException {

        System.err.println("Doc ID: "+sectionID+" "+documentIndices.get(sectionID));
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

        //System.out.println("Retain "+retain);

        Iterator<long[]> rowIter = document.nonZeroCoordinates().iterator();
        while (rowIter.hasNext()) {
            long[] coords = rowIter.next();
            double tfIdf = document.getAsDouble(coords);
            if (tfIdf > 0) {
                int col = (int) coords[1];
                if (retain.contains(col)) {
                    sortedTerms.add(new Pair<>(col, tfIdf));
                }
            }
        }

        TIntList highestN = new TIntArrayList();
        for (Pair<Integer, Double> pair : Iterables.limit(sortedTerms, n)) {
            highestN.add(pair.getKey());
        }

        //System.out.println("Highest n: "+highestN);

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

        BiMap<Integer, String> tokenIndicesInverse = tokenIndices.inverse();

        for (int i = 0; i < idxs.size(); i++) {
            terms.add(tokenIndicesInverse.get(idxs.get(i)));
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
            Iterator<long[]> rowIter = counts.selectRows(Calculation.Ret.LINK, cluster.get(i)).nonZeroCoordinates().iterator();
            TIntSet docTermSet = new TIntHashSet();
            while(rowIter.hasNext()) {
                //Why is coords[0] the column? Otherwise that would be the row?!
                docTermSet.add((int) rowIter.next()[0]);
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
        TIntList freqs = new TIntArrayList(); //the terms' cluster document frequencies (used as filter)
        double threshold = cluster.size()*ratio; //term has to occur in at least x percent of the documents
        System.out.println("Minimum cluster doc freq: "+threshold);

        for (int i = 0; i < cluster.size(); i++) {
            Iterator<long[]> rowIter = counts.selectRows(Calculation.Ret.LINK, cluster.get(i)).nonZeroCoordinates().iterator();
            while (rowIter.hasNext()) {
                long[] coords = rowIter.next();
                int termId = (int) coords[0];
                int idx = terms.binarySearch(termId);
                if (idx < 0) {
                    terms.add(termId);
                    freqs.add(1);
                } else {
                    freqs.set(idx, freqs.get(idx) + 1);
                }
            }
        }

        for (int j = 0; j < terms.size(); j++) {
            if (freqs.get(j) >= threshold) {
                partShared.add(terms.get(j));
            }
        }

        //System.out.println("Part shared: "+partShared);

        return partShared;
    }
}
