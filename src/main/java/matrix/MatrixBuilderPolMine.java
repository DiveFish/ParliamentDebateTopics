package matrix;

import clustering.KMeans;
import gnu.trove.list.TIntList;
import io.Layer;
import io.PolMineReader;
import io.VocabularyPolMine;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 *
 * @author Patricia Fischer
 */
public class MatrixBuilderPolMine extends MatrixBuilder {

    private static final int STOPWORD_LIST_SIZE = 150;//150;
    
    private static final Layer LAYER = Layer.TOKEN;  //options: TOKEN or LEMMA
    
    /*
     * If you hardcode the file directory, please uncomment: File filesDir = new File(FILE_DIR);
     * Otherwise, uncomment: File filesDir = new File(args[0]);
     */
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/bundesparser-xml-tokenized/";
    
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/bundesparser-xml-tokenized-samples/";
    
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/testFiles/";

    private static final String OUTPUT_DIR = "./PolMineMatrices/";
    
    /**
     *
     * @param args The directory where the xml files are stored
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        
        // Locale change necessary to display doubles with dot, not comma (messes up csv format)
        Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);
        
        if (args.length != 1) {
            System.out.println("Wrong number of arguments.Usage: 1, provide path to data files");
        }
        File filesDir = new File(args[0]);
        
        //File filesDir = new File(FILE_DIR);
        File[] files = filesDir.listFiles();
        
        
        // Read xml files and process them one after the other
        PolMineReader pol = new PolMineReader();
        VocabularyPolMine vocabulary = new VocabularyPolMine(LAYER);
        
        int filesDone = 0;
        for(File file : files) {
            if (file.isFile() && file.getName().endsWith(".xml")) {
                // Extract content of debates
                pol.constructDebate(file);
                // Process vocabulary of debates
                vocabulary.processFile(pol.getDebateID(), pol.getDebate());
            }
            System.out.println(++filesDone);
        }
        
        // Extract most frequent tokens from corpus
        Set<Integer> mostFrequent = mostFrequentTokens(vocabulary.tokenCounts(), STOPWORD_LIST_SIZE);
        PolMineMatrix tdm = new PolMineMatrix(LAYER, vocabulary.documentIndices(), vocabulary.tokenIndices(), mostFrequent);
        
        filesDone = 0;
        for(File file : files) {
            if (file.isFile() && file.getName().endsWith(".xml")) {
                // Extract content of debates
                pol.constructDebate(file);
                // Add term frequencies to matrix
                tdm.processFile(pol.getDebateID(), pol.getDebate());
            }
            System.out.println(++filesDone);
        }
        
        File directory = new File(OUTPUT_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }
        
        System.out.println("Saving term-document matrix to csv");
        try (PrintWriter pw1 = new PrintWriter(new File(OUTPUT_DIR+"TermDocumentMatrixPolMine.csv"))) {
            pw1.write(tdm.counts().toCSV());
            pw1.close();
        }
        
        // Transform term-document matrix into tf.idf matrix
        tdm.tfIdf(vocabulary.documentFrequencies());
        
        System.out.println("Saving tf.idf matrix to csv");
        try (PrintWriter pw2 = new PrintWriter(new File(OUTPUT_DIR+"TfIdfMatrixPolMine.csv"))) {
            pw2.write(tdm.counts().toCSV());
            pw2.close();
        }
        
        KMeans km = new KMeans(2, tdm.counts());
        System.out.println("Display k-means clusters");
        List<TIntList> clusters = km.clusters();
        for (TIntList cluster : clusters)
            System.out.println(cluster);
        
        /*
        // Decompose tf.idf matrix by applying singular-value decomposition
        SingularValueDecompositor svd = new SingularValueDecompositor(tdm.counts());
        System.out.println("Visualizing svd matrices");
        for(Matrix m : svd.decompose()) {
        System.out.println(m);
        }
         */
    }
    
}
