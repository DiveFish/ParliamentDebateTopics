package compact;

import gnu.trove.list.TIntList;
import gnu.trove.set.TIntSet;
import org.la4j.Vector;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Patricia Fischer
 */
public class main {
    private static final int NUM_OF_CLUSTERS = 3000;

    public static void main(String[] args) throws IOException {
        
        String corpus;
        File directory;
        
        //corpus = "taz";
        //directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz_samples/");
        //directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/taz/");
        
        
        //corpus = "PolMine";
        //directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/bundesparser-xml-tokenized/");
        //directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/bundesparser-xml-tokenized-samples/");
        //directory = new File("/home/patricia/NetBeansProjects/ParliamentDebateTopics/testFiles/");
        
        
        
        if (args.length != 2) {
            System.out.println("Wrong number of arguments.Usage: 2, provide name of dataset (Polmine/Taz) path to data files");
        }
        corpus = args[0].toLowerCase();
        directory = new File(args[1]);
        
        MatrixBuilder mb = new MatrixBuilder();
        Vocabulary vocabulary = mb.buildVocabulary(corpus, directory);
        TermDocumentMatrix tdm = mb.buildMatrix(corpus, directory, vocabulary);

        System.out.println("_________________________");
        System.out.println("Retrieve k-means clusters");
        System.out.println("_________________________");
        KMeansClustering kmc = new KMeansClustering(NUM_OF_CLUSTERS, tdm.counts(), new Random());
        List<Vector> centroids = kmc.centroids();
        
        List<TIntList> clusters = kmc.clusters(centroids);
        for (TIntList cluster : clusters) {
            TIntSet sharedTerms = tdm.sharedTerms(cluster);

            System.out.println(cluster);
            for (Integer c : cluster.toArray()) {
                tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, vocabulary.tokenIndices(), sharedTerms);
            }
            System.out.println();
        }
        
        System.out.println("____________________");
        System.out.println("Find best clustering");
        System.out.println("____________________");
        List<TIntList> bestClusters = kmc.nClusters(3);
        for (TIntList cluster : bestClusters) {
            TIntSet sharedTerms = tdm.sharedTerms(cluster);

            System.out.println(cluster);
            for (Integer c : cluster.toArray()) {
                tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, vocabulary.tokenIndices(), sharedTerms);
            }
            System.out.println();
        }
    }
    
}
