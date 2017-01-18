package parliamentdebatetopics;

import gnu.trove.list.TIntList;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.annolab.tt4j.TreeTaggerException;
import org.ujmp.core.Matrix;

/**
 * Construct a term-document and tf.idf matrix and svd matrices from the 
 * Polmine corpus data. First, read the xml data into debate objectst,
 * then extract the vocabulary as lemmas or tokens, exclude stop words
 * selected by high frequency and finally use vocabulary and debates to
 * create matrices of the input documents.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class MatrixBuilder {

    private static final int STOPWORD_LIST_SIZE = 0; //150

    public static void main(String[] args) throws IOException, TreeTaggerException {
        
        // Read xml files and save them into debate objects
        PolmineReader pol = new PolmineReader();
        pol.constructDebates();
        
        // Process vocabulary of debates
        String layer = "form";  //"lemma" to process text by types, "form" to process text by tokens
        Vocabulary vocabulary = new Vocabulary(layer);
        vocabulary.processDebates(pol.getDebates());

        Set<Integer> mostFrequent = mostFrequentTokens(vocabulary.tokenCounts(), STOPWORD_LIST_SIZE);
        
        // Create term-document matrix of debates
        TermDocumentMatrix termDocMatrix = new TermDocumentMatrix(layer, vocabulary.documentIndices(),
                vocabulary.tokenIndices(), mostFrequent);
        termDocMatrix.processDebates(pol.getDebates());
        Matrix termDocumentMatrix = termDocMatrix.counts();
        System.out.println("Visualizing term document matrix");
        System.out.println(termDocumentMatrix);
        
        // Transform term-document matrix into td.idf matrix
        Matrix tfIdfMatrix = termDocMatrix.tfIdf(termDocumentMatrix);
        System.out.println("Visualizing td.idf matrix");
        System.out.println(tfIdfMatrix);
        
        /*
        // Decompose tf.idf matrix by applying singular-value decomposition        
        Matrix[] svdMatrix = tdfIdfMatrix.svd();
        System.out.println("Visualizing svd matrices");
        for(Matrix m : svdMatrix){
            System.out.println(m);
        }
        */
        
    }

    /**
     * Find the N most frequent tokens.
     *
     * @param tokenFreqs Token frequencies.
     * @param n          The number of most frequent tokens to find.
     * @return Most frequent tokens (by index).
     */
    private static Set<Integer> mostFrequentTokens(TIntList tokenFreqs, int n) {
        FreqComparator comparator = new FreqComparator(tokenFreqs);
        BestN<Integer> bestN = new BestN<>(n, comparator);

        for (int i = 0; i < tokenFreqs.size(); i++) {
            bestN.add(i);
        }
        
        return new HashSet<>(bestN);
    }

    /**
     * Comparator for tokens (by index). A token with a higher frequency is sorted
     * before a token with a lower frequency.
     */
    private static class FreqComparator implements Comparator<Integer> {
        private final TIntList frequencies;

        private FreqComparator(TIntList frequencies) {
            this.frequencies = frequencies;
        }

        @Override
        public int compare(Integer idx1, Integer idx2) {
            int freqCmp = Integer.compare(frequencies.get(idx2), frequencies.get(idx1));
            if (freqCmp != 0)
                return freqCmp;

            return Integer.compare(idx1, idx2);
        }
    }
}
