package compact;

import gnu.trove.list.TIntList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Vocabulary buildVocabulary(String corpus, File directory) throws IOException {
        String fileExtension = getExtension(corpus);
        Reader read = getReader(corpus);
        List<File> files = getFiles(corpus, directory, fileExtension);


        Vocabulary vocabulary = new Vocabulary(LAYER);

        int filesDone = 0;
        for (File file : files) {
            // Extract content of section(s)
            read.processFile(file);
            // Process vocabulary of section(s)
            for (int i = 0; i < read.getSectionIDs().size(); i++) {
                vocabulary.processSection(read.getSectionIDs().get(i), read.getContent().get(i));
            }
            System.out.println(++filesDone);
        }

        return vocabulary;
    }

    private List<File> getFiles(String corpus, File directory, String fileExtension) {
        List<File> files = new ArrayList();
        if (corpus.equalsIgnoreCase("taz")) {
            for (File dir : directory.listFiles()) {
                File subDir = new File(dir.getAbsolutePath());
                for (File file : subDir.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(fileExtension)) {
                        files.add(file);
                    }
                }
            }
        } else if (corpus.equalsIgnoreCase("PolMine")) {
            for (File file : directory.listFiles()) {
                if (file.isFile() && file.getName().endsWith(fileExtension)) {
                    files.add(file);
                }
            }
        } else {
            System.err.println("Provide a corpus name, choose between PolMine and taz.");
        }
        return files;
    }

    private Reader getReader(String corpus) throws IOException {
        Reader read = new ReaderPolMine(LAYER);
        if (corpus.equalsIgnoreCase("taz")) {
            read = new ReaderTaz(LAYER);
        } else if (corpus.equalsIgnoreCase("PolMine")) {
            read = new ReaderPolMine(LAYER);
        } else {
            System.err.println("Provide a corpus name, choose between PolMine and taz.");
        }
        return read;
    }

    private String getExtension(String corpus) {
        String fileExtension = "";
        if (corpus.equalsIgnoreCase("taz")) {
            fileExtension = ".conll.gz";
        } else if (corpus.equalsIgnoreCase("PolMine")) {
            fileExtension = ".xml";
        } else {
            throw new IllegalArgumentException("Unknown corpus type:" + corpus);
        }
        return fileExtension;
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
        Reader read = getReader(corpus);
        List<File> files = getFiles(corpus, directory, fileExtension);

        Set<Integer> mostFrequent = mostFrequentTokens(vocabulary.tokenCounts(), STOPWORD_LIST_SIZE);
        TermDocumentMatrix tdm = new TermDocumentMatrix(vocabulary.documentIndices(), vocabulary.tokenIndices(), mostFrequent);

        int filesDone = 0;
        for (File file : files) {
            // Extract content of section(s)
            read.processFile(file);
            // Add term frequencies to matrix
            for (int i = 0; i < read.getSectionIDs().size(); i++) {
                tdm.processSection(read.getSectionIDs().get(i), read.getContent().get(i));
            }
            System.out.println(++filesDone);
        }

        // Transform term-document matrix into tf.idf matrix
        System.err.println("Calculating tf.idf matrix...");
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
