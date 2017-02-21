package compact;

import gnu.trove.list.TIntList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.la4j.Vector;
import org.la4j.iterator.MatrixIterator;

/**
 * Construct a term-document and tf.idf matrix and svd matrices from given
 * corpus. First, read the data immediately excluding stopwords, then extract
 * the vocabulary as lemmas or tokens to create matrices of the input documents.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class MatrixBuilder {
    
    private static final Layer LAYER = Layer.TOKEN;  //TOKEN or LEMMA
    
    private static final int STOPWORD_LIST_SIZE = 400;//150;
    
    private static final int NUM_OF_CLUSTERS = 10;
        
    /**
     *
     * @param corpus The name of the corpus, either PolMine or taz
     * @param directory The directory of the data collection
     * @throws IOException
     */
    public void buildMatrix(String corpus, File directory) throws IOException {
        
        // Locale change necessary to display doubles with dot, not comma (messes up csv format)
        Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);
        
        List<File> files = new ArrayList();
        String fileExtension = "";
        Reader read = new ReaderPolMine(LAYER);
        
        if (corpus.equalsIgnoreCase("taz")) {
            read = new ReaderTaz(LAYER);
            fileExtension = ".conll.gz";
            for (File dir : directory.listFiles()) {
                File subDir = new File(dir.getAbsolutePath());
                files.addAll(Arrays.asList(subDir.listFiles()));
            }
        }
        else if (corpus.equalsIgnoreCase("PolMine")) {
            read = new ReaderPolMine(LAYER);
            files.addAll(Arrays.asList(directory.listFiles()));
            fileExtension = ".xml";
        }
        else {
            System.out.println("Provide a corpus name, choose between PolMine and taz.");
        }
        
        // Read files and process them one after the other
        
        Vocabulary vocabulary = new Vocabulary(LAYER);
        
        int filesDone = 0;
        for(File file : files) {
            if (file.isFile() && file.getName().endsWith(fileExtension)) {
                // Extract content of section(s)
                read.processFile(file);
                // Process vocabulary of section(s)
                for (int i = 0; i < read.getSectionIDs().size(); i++) {
                    vocabulary.processSection(read.getSectionIDs().get(i), read.getContent().get(i));
                }
            }
            System.out.println(++filesDone);
        }
        
        // Extract most frequent tokens from corpus
        Set<Integer> mostFrequent = mostFrequentTokens(vocabulary.tokenCounts(), STOPWORD_LIST_SIZE);
        TermDocumentMatrix tdm = new TermDocumentMatrix(vocabulary.documentIndices(), vocabulary.tokenIndices(), mostFrequent);
        
        filesDone = 0;
        for(File file : files) {
            if (file.isFile() && file.getName().endsWith(fileExtension)) {
                // Extract content of section(s)
                read.processFile(file);
                // Add term frequencies to matrix
                for (int i = 0; i < read.getSectionIDs().size(); i++) {
                    tdm.processSection(read.getSectionIDs().get(i), read.getContent().get(i));
                }
            }
            System.out.println(++filesDone);
        }
        
        // Transform term-document matrix into tf.idf matrix
        System.out.println("Calculate tf.idf matrix");
        tdm.tfIdf(vocabulary.documentFrequencies());
        
        System.out.println("Retrieve k-means clusters");
        KMeansClustering kmc = new KMeansClustering(NUM_OF_CLUSTERS, tdm.counts());
        List<Vector> centroids = kmc.centroids();
        List<TIntList> clusters = kmc.clusters(centroids);
        for (TIntList cluster : clusters) {
            System.out.println(cluster);
            for (Integer c : cluster.toArray()) {
                tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, vocabulary.tokenIndices());
            }
            System.out.println();
        }
    }
    
    /**
     * Find the N most frequent tokens.
     *
     * @param tokenFreqs Token frequencies.
     * @param n The number of most frequent tokens to find.
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
