package debatetopics;

import gnu.trove.list.TIntList;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.la4j.Matrix;
import org.la4j.decomposition.SingularValueDecompositor;
import org.la4j.matrix.SparseMatrix;

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

    private static final int STOPWORD_LIST_SIZE = 150;//150;
    private static final int MAX_VOCAB_SIZE = 500000;
    private static final Layer LAYER = Layer.TOKEN;  //options: TOKEN or LEMMA
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/src/bundesparser-xml-tokenized/";
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/src/bundesparser-xml-tokenized-samples/";
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/src/testFiles/";

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        
        // Locale change necessary to display doubles with dot, not comma (messes up csv format)
        Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);
        
        if (args.length != 1){
            System.out.println("Wrong number of arguments.\nUsage: 1, provide path to data files");
        }
        
        File filesDir = new File(args[0]);//(FILE_DIR);
        File[] files = filesDir.listFiles();
        
        // Read xml files and save them into debate objects
        PolmineReader pol = new PolmineReader();
        Vocabulary vocabulary = new Vocabulary(LAYER);
        TermDocumentMatrix tdm = new TermDocumentMatrix(LAYER, files.length, MAX_VOCAB_SIZE);
        
        for(File file : files){
            if (file.isFile() && file.getName().endsWith(".xml")){
                // Extract content of debates
                pol.constructDebate(file);
                // Process vocabulary of debates
                vocabulary.processDebate(pol.getFileID(), pol.getDebate());
                // Create document vector and add to matrix
                tdm.processDebate(pol.getFileID(), pol.getDebate(), vocabulary.documentIndices(), vocabulary.tokenIndices());
            }
        }
        
        tdm.removeEmptyRows(vocabulary.tokenIndices().size()-1);
        
        // Remove all words with high frequency/ stopwords
        Set<Integer> mostFrequent = mostFrequentTokens(vocabulary.tokenCounts(), STOPWORD_LIST_SIZE);
        tdm.removeMostFrequent(mostFrequent);
        
        System.out.println("Visualizing term document matrix");
        System.out.println(tdm.counts());
        //System.out.println(tdm.counts().toCSV());
                
        System.out.println("Saving term document matrix to csv");
        try (PrintWriter pw1 = new PrintWriter(new File("./TermDocMatrix.csv"))) {
            pw1.write(tdm.counts().toCSV());
        }
        
        // Transform term-document matrix into td.idf matrix
        tdm.tfIdf(vocabulary.documentFrequencies());
        System.out.println("Visualizing td.idf matrix");
        System.out.println(tdm.counts());
        
        System.out.println("Saving tf.idf matrix to csv");
        try (PrintWriter pw2 = new PrintWriter(new File("TfIdfMatrices.csv"))) {
            pw2.write(tdm.counts().toCSV());
            pw2.close();
        }
        /*
        // Decompose tf.idf matrix by applying singular-value decomposition
        SingularValueDecompositor svd = new SingularValueDecompositor(tdm.counts());
        System.out.println("Visualizing svd matrices");
        for(Matrix m : svd.decompose()){
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