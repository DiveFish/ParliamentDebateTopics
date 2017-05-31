package compact;

import static compact.ReaderUtils.*;
import gnu.trove.list.TIntList;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Construct a term-document and tf-idf matrix and svd matrices from given
 * corpus. First, read the data immediately excluding stopwords, then extract
 * the vocabulary as lemmas or tokens to create matrices of the input documents.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class MatrixBuilder {

    private static final Layer LAYER = Layer.LEMMA;  //TOKEN or LEMMA

    public Vocabulary buildVocabulary(String corpus, File directory) throws IOException {
        String fileExtension = getExtension(corpus);
        Reader read = getReader(corpus, LAYER);
        List<File> files = getFiles(corpus, directory, fileExtension);

        Vocabulary vocabulary = new Vocabulary();

        int filesDone = 0;
        for (File file : files) {
            // Extract content of section(s)
            read.processFile(file);
            // Process vocabulary of section(s)
            for (int i = 0; i < read.getSectionIDs().size(); i++) {
                vocabulary.processSection(read.getSectionIDs().get(i), read.getContent().get(i));
            }
            System.err.println(++filesDone);
        }
        vocabulary.extractDocumentDates(read.getMetadata());

        // Sentence and token counts for all sections
        System.out.println("Sentence count, token count");
        for (Map.Entry<String, List<String>> entry : read.getMetadata().entrySet()) {
                System.out.println(entry.getValue().get(1) + ", " + entry.getValue().get(2));
        }

        return vocabulary;
    }
    
    /**
     * @param corpus     The name of the corpus, either PolMine or taz
     * @param directory  The directory of the data collection
     * @param vocabulary
     * @return 
     * @throws IOException
     */
    public TermDocumentMatrix buildMatrix(String corpus, File directory, Vocabulary vocabulary) throws IOException {

        // Locale change necessary to display doubles with dot, not comma (messes up csv format)
        //Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);

        String fileExtension = getExtension(corpus);
        Reader read = getReader(corpus, LAYER);
        List<File> files = getFiles(corpus, directory, fileExtension);

        //Set<Integer> mostFrequent = mostFrequentTokens(vocabulary.tokenCounts(), STOPWORD_LIST_SIZE);
        TermDocumentMatrix tdm = new TermDocumentMatrix(vocabulary.documentIndices(), vocabulary.tokenIndices());

        int filesDone = 0;
        for (File file : files) {
            // Extract content of section(s)
            read.processFile(file);
            // Add term frequencies to matrix
            for (int i = 0; i < read.getSectionIDs().size(); i++) {
                tdm.processSection(read.getSectionIDs().get(i), read.getContent().get(i));
            }
            System.err.println(++filesDone);
        }

        // Transform term-document matrix into tf-idf matrix
        System.err.println("Calculating tf-idf matrix...");
        tdm.tfIdf(vocabulary.documentFrequencies());

        return tdm;
    }

    /**
     * Find the N most frequent tokens.
     *
     * @param tokenFreqs Token frequencies.
     * @param n          The number of most frequent tokens to find.
     * @return Most frequent tokens (by index).
     */
    protected static Set<Integer> mostFrequentTokens(TIntList tokenFreqs, int n) {
        FreqComparator comparator = new FreqComparator(tokenFreqs);
        BestN<Integer> bestN = new BestN(n, comparator);

        for (int i = 0; i < tokenFreqs.size(); i++) {
            bestN.add(i);
        }

        return new HashSet(bestN);
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
