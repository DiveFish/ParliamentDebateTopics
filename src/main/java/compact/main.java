package compact;

import com.google.common.collect.BiMap;
import gnu.trove.list.TIntList;
import gnu.trove.set.TIntSet;
import org.la4j.Vector;
import org.ujmp.core.doublematrix.SparseDoubleMatrix2D;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Patricia Fischer
 */
public class main {
    private static final int NUM_OF_CLUSTERS = 1; //800 //3000;

    private static double RATIO = 0.3;

    public static void main(String[] args) throws IOException {
        
        String corpus;
        File directory;
        String storageDirectory;
        storageDirectory = "/tmp/";
        
        //corpus = "taz";
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/taz-sample/");  // 12 files -> 677 sections
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/taz/");
        
        
        //corpus = "PolMine";
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/bundesparser-xml-tokenized/"); // 984 debates -> 215.080 sections
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/bundesparser-xml-tokenized-sample/"); // 12 debates -> 2733 sections
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/test/");
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/PolTest/");

        if (args.length != 3) {
            System.out.println("Wrong number of arguments.Usage: 3, provide name of dataset (Polmine/Taz), path to data files and path for storage");
        }
        corpus = args[0].toLowerCase();
        directory = new File(args[1]);
        storageDirectory = args[2];
        
        MatrixBuilder mb = new MatrixBuilder();
        Vocabulary vocabulary = mb.buildVocabulary(corpus, directory);
        TermDocumentMatrix tdm = mb.buildMatrix(corpus, directory, vocabulary); //1. term-doc matrix -> 2. tf-idf matrix
        // Mapping from doc ID to doc name
        BiMap<Integer, String> documentIndicesInverted = vocabulary.documentIndices().inverse();
        
        //tdm.svd();
        
        System.out.println("_________________________");
        System.out.println("Retrieve k-means clusters");
        System.out.println("_________________________");
        KMeansClustering kmc = new KMeansClustering(NUM_OF_CLUSTERS, tdm.counts(), new Random());
        List<Vector> centroids = kmc.centroids();
        
        List<TIntList> clusters = kmc.clusters(centroids);
        for (TIntList cluster : clusters) {
            if (cluster.isEmpty()) {
                System.out.println("Empty cluster");
            }
            else {
                // Earliest doc in cluster
                System.out.println(documentIndicesInverted.get(kmc.earliestDoc(vocabulary.documentDates(), cluster)));

                //TIntSet sharedTerms = tdm.sharedTerms(cluster);
                TIntSet sharedTerms = tdm.partiallySharedTerms(cluster, RATIO);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, vocabulary.tokenIndices(), sharedTerms);
                }
            }
            System.out.println();
        }
        /*
        System.out.println("____________________");
        System.out.println("Best clustering");
        System.out.println("____________________");
        List<TIntList> bestClusters = kmc.nClusters(3);
        for (TIntList cluster : bestClusters) {
            
            if (cluster.isEmpty()) {
                System.out.println("Empty cluster");
            }
            else {
                // Earliest doc in cluster
                System.out.println(documentIndicesInverted.get(kmc.earliestDoc(vocabulary.documentDates(), cluster)));

                //TIntSet sharedTerms = tdm.sharedTerms(cluster);
                TIntSet sharedTerms = tdm.partiallySharedTerms(cluster, RATIO);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, vocabulary.tokenIndices(), sharedTerms);
                }
            }
            System.out.println();
        }
        */

        SparseDoubleMatrix2D serializableMatrix = SparseDoubleMatrix2D.Factory.zeros(0, 0);
        SparseDoubleMatrix2D serializableCentroids = SparseDoubleMatrix2D.Factory.zeros(0, 0);

        List<MatrixValue> countVals = new ArrayList<>();
        List<List<MatrixValue>> centroidVals = new ArrayList<>();
        //StorageInformation info = new StorageInformation(serializableMatrix, serializableCentroids, vocabulary.documentIndices(), vocabulary.documentDates());
        StorageInformation info = new StorageInformation(countVals, tdm.counts().rows(), tdm.counts().columns(), centroidVals, vocabulary.documentIndices(), vocabulary.documentDates());
        //info.matrixToSerializable(tdm.counts());
        //info.centroidsToSerializable(centroids);

        info.countsToSerializable(tdm.counts());
        info.centroidListToSerializable(centroids);

        Storage store = new Storage(storageDirectory);
        store.setStorageInfo(info);
        store.serialize();
        store.deserialize();

        //info.serializableToCentroids();
        //info.serializableToMatrix();
        info.serializableToCounts();
        info.serializableToCentroidList();;
        System.out.println("TFIDF   Rows/#docs: "+info.getCountsMatrix().rows()+", columns/#words: "+info.getCountsMatrix().columns());
        System.out.println("CENTROIDS   Rows/#centroids: "+info.getCentroids().size()+", columns/#words: "+info.getCentroids().get(0).length());
        System.out.println("First doc date: "+info.getDocumentDates().get(0));
        System.out.println("First doc filename: "+info.getDocumentIndices().inverse().get(0));
    }
    
}
