package matrix;

import io.Layer;
import io.TazReader;
import io.VocabularyTaz;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author Patricia Fischer
 */
public class MatrixBuilderTaz extends MatrixBuilder {
    
    private static final int STOPWORD_LIST_SIZE = 100;//150;
    
    private static final Layer LAYER = Layer.TOKEN;  //options: TOKEN or LEMMA
    
    /*
     * If you hardcode the file directory, please uncomment: File filesDir = new File(FILE_DIR);
     * Otherwise, uncomment: File filesDir = new File(args[0]);
     */
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz/1988/";
    
    private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz/";
    
    private static final String OUTPUT_DIR = "./serializedMatrix.txt" ;
    
    
    public static void main(String[] args) throws IOException {
        
        File mainDir = new File(FILE_DIR);
        File[] directories = mainDir.listFiles();
        List<File> files = new ArrayList<>();
        for (File dir : directories) {
            File subDir = new File(dir.getAbsolutePath());
            files.addAll(Arrays.asList(subDir.listFiles()));
        }
        
        // Read conll files from all subdirectories and process them one after the other
        TazReader taz = new TazReader();
        VocabularyTaz vocabulary = new VocabularyTaz(LAYER);
        
        int filesDone = 0;
        for(File file : files) {
            if (file.isFile() && file.getName().endsWith(".conll.gz")) {
                // Extract article content
                taz.processFile(file);
                vocabulary.processFile(taz.getArticleID(), taz.getArticleContent());
            }
            System.out.println(++filesDone);
        }
        
        // Extract most frequent tokens from corpus
        Set<Integer> mostFrequent = mostFrequentTokens(vocabulary.tokenCounts(), STOPWORD_LIST_SIZE);
        TazMatrix tdm = new TazMatrix(LAYER, vocabulary.documentIndices(), vocabulary.tokenIndices(), mostFrequent);
        
        filesDone = 0;
        for(File file : files) {
            if (file.isFile() && file.getName().endsWith(".conll.gz")) {
                // Extract article content
                taz.processFile(file);
                // Add term frequencies to matrix
                tdm.processFile(taz.getArticleID(), taz.getArticleContent());
            }
            System.out.println(++filesDone);
        }
        
        System.out.println("Saving term-document matrix to csv");
        try (PrintWriter pw1 = new PrintWriter(new File("./TermDocumentMatrixTaz.csv"))) {
            pw1.write(tdm.counts().toCSV());
            pw1.close();
        }
        
        // Transform term-document matrix into tf.idf matrix
        tdm.tfIdf(vocabulary.documentFrequencies());
        System.out.println("Saving tf.idf matrix to csv");
        try (PrintWriter pw2 = new PrintWriter(new File("TfIdfMatrixTaz.csv"))) {
            pw2.write(tdm.counts().toCSV());
            pw2.close();
        }
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
