package matrix;

import gnu.trove.list.TIntList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

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

    /**
     * Find the N most frequent tokens.
     *
     * @param tokenFreqs Token frequencies.
     * @param n          The number of most frequent tokens to find.
     * @return Most frequent tokens (by index).
     */
    protected static Set<Integer> mostFrequentTokens(TIntList tokenFreqs, int n) {
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
