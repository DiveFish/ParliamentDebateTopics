package ujmp_trial;

import com.google.common.collect.BiMap;
import gnu.trove.list.TIntList;
import gnu.trove.set.TIntSet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.doublematrix.SparseDoubleMatrix;

/**
 *
 * @author Patricia Fischer
 */
public class main {
    private static final int NUM_OF_CLUSTERS = 500;//3000;

    public static void main(String[] args) throws IOException {
        System.out.printf("Running %s\n", main.class);
        String corpus;
        File directory;
        
        //corpus = "taz";
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/taz-sample/");
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/taz/");
        
        
        //corpus = "PolMine";
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/bundesparser-xml-tokenized-minisample/");
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/bundesparser-xml-tokenized-sample/");
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/bundesparser-xml-tokenized/");
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/testFiles/");
        

        if (args.length != 2) {
            System.out.println("Wrong number of arguments.Usage: 2, provide name of dataset (Polmine/Taz) path to data files");
        }
        corpus = args[0].toLowerCase();
        directory = new File(args[1]);
        
        MatrixBuilder mb = new MatrixBuilder();
        Vocabulary vocabulary = mb.buildVocabulary(corpus, directory);
        TermDocumentMatrix tdm = mb.buildMatrix(corpus, directory, vocabulary);

        System.out.println("_________________________\n");
        System.out.println("Retrieve k-means clusters");
        System.out.println("_________________________");

        BiMap<Integer, String> documentIndicesInverted = vocabulary.documentIndices().inverse();

        KMeansClustering kmc = new KMeansClustering(NUM_OF_CLUSTERS, tdm.counts(), new Random());
        SparseDoubleMatrix centroids = kmc.centroids();
        
        List<TIntList> clusters = kmc.clusters(centroids);
        for (TIntList cluster : clusters) {

            if (cluster.isEmpty()) {
                System.out.println("Empty cluster");
            }
            else {
                // Earliest doc in cluster
                System.out.println(documentIndicesInverted.get(kmc.earliestDoc(vocabulary.documentDates(), cluster)));

                TIntSet sharedTerms = tdm.sharedTerms(cluster);
                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms((SparseDoubleMatrix) tdm.counts().selectRows(Calculation.Ret.NEW, c).toDoubleMatrix(), 10, vocabulary.tokenIndices(), sharedTerms);
                }
                System.out.println();
            }
        }
        
        
        
        System.out.println("____________________\n");
        System.out.println("Best clustering");
        System.out.println("____________________");
        List<TIntList> bestClusters = kmc.nClusters(2); //TODO: change back to higher number of iterations after testing

        for (TIntList cluster : bestClusters) {
            
            if (cluster.isEmpty()) {
                System.out.println("Empty cluster");
            }
            else {
                // Earliest doc in cluster
                System.out.println(documentIndicesInverted.get(kmc.earliestDoc(vocabulary.documentDates(), cluster)));

                TIntSet sharedTerms = tdm.sharedTerms(cluster);
                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms((SparseDoubleMatrix) tdm.counts().selectRows(Calculation.Ret.NEW, c), 10, vocabulary.tokenIndices(), sharedTerms);
                }
            }
            System.out.println();
        }
        
    }
    
}
