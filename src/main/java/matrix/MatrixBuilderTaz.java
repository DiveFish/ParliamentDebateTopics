package matrix;

import io.Layer;
import io.TazReader;
import io.VocabularyTaz;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author Patricia Fischer
 */
public class MatrixBuilderTaz extends MatrixBuilder {
    
    private static final int STOPWORD_LIST_SIZE = 10;//50;//150;
    
    private static final Layer LAYER = Layer.TOKEN;  //options: TOKEN or LEMMA
    
    /*
     * If you hardcode the file directory, please uncomment: File filesDir = new File(FILE_DIR);
     * Otherwise, uncomment: File filesDir = new File(args[0]);
     */
    //private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz/1988/";
    
    private static final String FILE_DIR = "/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz/sample/";
    
    
    public static void main(String[] args) throws IOException {
        
        File filesDir = new File(FILE_DIR);
        File[] files = filesDir.listFiles();
        
        // Read conll files and process them one after the other
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
                // Extract content of debates
                taz.processFile(file);
                // Add term frequencies to matrix
                tdm.processFile(taz.getArticleID(), taz.getArticleContent());
            }
            System.out.println(++filesDone);
        }
        
        System.out.println("Visualizing term-document matrix");
        System.out.println(tdm.counts());
        
        System.out.println("Saving term-document matrix to csv");
        try (PrintWriter pw1 = new PrintWriter(new File("./TermDocumentMatrixTaz.csv"))) {
            pw1.write(tdm.counts().toCSV());
        }
        
        // Transform term-document matrix into tf.idf matrix
        tdm.tfIdf(vocabulary.documentFrequencies());
        System.out.println("Visualizing tf.idf matrix");
        System.out.println(tdm.counts());
        /*
        System.out.println("Saving tf.idf matrix to csv");
        try (PrintWriter pw2 = new PrintWriter(new File("TfIdfMatrixTaz.csv"))) {
            pw2.write(tdm.counts().toCSV());
            pw2.close();
        }
        
        // Decompose tf.idf matrix by applying singular-value decomposition
        SingularValueDecompositor svd = new SingularValueDecompositor(tdm.counts());
        System.out.println("Visualizing svd matrices");
        for(Matrix m : svd.decompose()) {
            System.out.println(m);
        }
        */
    }
    
}
