package compact;

import com.carrotsearch.hppc.BitSet;
import com.google.common.collect.BiMap;
import gnu.trove.list.TIntList;
import gnu.trove.set.TIntSet;
import org.apache.commons.math3.random.MersenneTwister;
import org.la4j.Vector;
import org.la4j.vector.SparseVector;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static compact.ClusterUtils.*;

/**
 *
 * @author Patricia Fischer
 */
public class main {
    private static final int NUM_OF_CLUSTERS = 100; //800 //3000;

    private static final int NUM_OF_BITS = 1200; //1024 --> 528 --> 2048

    private static double ratio = 0.3;

    public static void main(String[] args) throws IOException {
        
        String corpus;
        File directory;
        String storageDirectory;
        //storageDirectory = "/home/patricia/Dokumente/Bachelorarbeit/Results/taz/Samples/";
        
        //corpus = "taz";
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/taz-sample1/");  // 12 files -> 677 sections (309 for articles >10 sentences)
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/taz/"); //647413 sections for articles >10 sentences

        //corpus = "PolMine";
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/bundesparser-xml-tokenized/"); // 984 debates -> 215.080 sections
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/bundesparser-xml-tokenized-sample/"); // 12 debates -> 2733 sections
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/test/");
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/bundesparser-conll-xml/");
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/bundesparser-conll-xml-sample/");
        //directory = new File("/home/patricia/Dokumente/Bachelorarbeit/Corpora/bundesparser-conll-xml-minisample/");

        if (args.length != 3) {
            System.err.println("Wrong number of arguments.Usage: 3, provide name of dataset (Polmine/Taz), path to data files and path for storage");
        }

        corpus = args[0].toLowerCase();
        directory = new File(args[1]);
        storageDirectory = args[2];


        ///*-->
        MatrixBuilder mb = new MatrixBuilder();
        Vocabulary vocabulary = mb.buildVocabulary(corpus, directory);
        TermDocumentMatrix tdm = mb.buildMatrix(corpus, directory, vocabulary); // creates term-doc matrix, returns tf-idf matrix
        BiMap<Integer, String> documentIndicesInverted = vocabulary.documentIndices().inverse(); //doc id <-> doc name
        //-->*/

        //tdm.svd();



        ///*-->

        System.out.println("_________________________");
        System.out.println("Retrieving k-means clusters");
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
                System.out.println(documentIndicesInverted.get(earliestDoc(vocabulary.documentDates(), cluster)));


                ratio = 0.3;
                System.out.printf("--%f ratio--", ratio);
                //TIntSet sharedTerms = tdm.sharedTerms(cluster);
                TIntSet sharedTerms = tdm.partiallySharedTerms(cluster, ratio);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, vocabulary.tokenIndices(), sharedTerms);
                }


                ratio = 0.5;
                System.out.printf("--%f ratio--\n", ratio);
                //TIntSet sharedTerms = tdm.sharedTerms(cluster);
                sharedTerms = tdm.partiallySharedTerms(cluster, ratio);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, vocabulary.tokenIndices(), sharedTerms);
                }


                ratio = 0.7;
                System.out.printf("--%f ratio--\n", ratio);
                //TIntSet sharedTerms = tdm.sharedTerms(cluster);
                sharedTerms = tdm.partiallySharedTerms(cluster, ratio);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, vocabulary.tokenIndices(), sharedTerms);
                }


                ratio = 1.0;
                System.out.printf("--%f ratio--\n", ratio);
                //TIntSet sharedTerms = tdm.sharedTerms(cluster);
                sharedTerms = tdm.partiallySharedTerms(cluster, ratio);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, vocabulary.tokenIndices(), sharedTerms);
                }
            }
            System.out.println();
        }



        //Serialize/deserialize

        List<MatrixValue> countValues = new ArrayList<>();
        List<List<MatrixValue>> centroidValues = new ArrayList<>();
        StorageInformation info = new StorageInformation(countValues, tdm.counts().rows(), tdm.counts().columns(),
                centroidValues, vocabulary.documentIndices(), vocabulary.tokenIndices(), vocabulary.documentDates());

        info.matrixToSerializable(tdm.counts());
        info.centroidsToSerializable(centroids);

        Storage store = new Storage(storageDirectory);
        store.setStorageInfo(info);
        store.serialize();
        /*
        store.deserialize();

        info.serializableToMatrix();
        info.serializableToCentroids();;
        System.out.println("TFIDF   Rows/#docs: "+info.getCountsMatrix().rows()+", columns/#words: "+info.getCountsMatrix().columns());
        System.out.println("CENTROIDS   Rows/#centroids: "+info.getCentroids().size()+", columns/#words: "+info.getCentroids().get(0).length());
        System.out.println("First doc date: "+info.getDocumentDates().get(0));
        System.out.println("First doc filename: "+info.getDocumentIndices().inverse().get(0));
*/


/*
        System.out.println("_________________________");
        System.out.printf("Hashing %d vectors...\n", vocabulary.documentIndices().size());
        System.out.println("_________________________");
        RandomProjectionHash rph = new RandomProjectionHash(new MersenneTwister(42), vocabulary.tokenIndices().size(), NUM_OF_BITS); //bits: 1024
        List<BitSet> hashes = new ArrayList<>();
        for (int i = 0; i < tdm.counts().rows(); i++) {
            SparseVector row = tdm.counts().getRow(i).toSparseVector();
            BitSet hash = rph.hashVector(row);
            hashes.add(hash);
        }
        HashedDocuments hashedDocuments = new HashedDocuments(vocabulary.documentIndices(), hashes, NUM_OF_BITS);

        KMeansHashClustering hashClustering = new KMeansHashClustering(NUM_OF_CLUSTERS, hashedDocuments, new Random());
        List<BitSet> bitCentroids = hashClustering.centroids();

        List<TIntList> bitClusters = hashClustering.clusters(bitCentroids);
        for (TIntList cluster : bitClusters) {
            if (cluster.isEmpty()) {
                System.out.println("Empty cluster");
            }
            else {
                // Earliest doc in cluster
                System.out.println(documentIndicesInverted.get(kmc.earliestDoc(vocabulary.documentDates(), cluster)));

                TIntSet sharedTerms = tdm.partiallySharedTerms(cluster, ratio);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, vocabulary.tokenIndices(), sharedTerms);
                }
            }
            System.out.println();
        }
*/
        //-->*/


/*
        //Remainder of main() is meant for deserializing information from existing storage.ser
        Storage store = new Storage(storageDirectory);
        store.deserialize();
        StorageInformation info = store.getStorageInfo();
        info.serializableToMatrix();
        info.serializableToCentroids();

        Map<Integer, Date> documentDates = info.getDocumentDates();

        BiMap<String, Integer> tokenIndices = info.getTokenIndices();

        BiMap<String, Integer> documentIndices = info.getDocumentIndices();
        BiMap<Integer, String> documentIndicesInverted = documentIndices.inverse();

        TermDocumentMatrix tdm = new TermDocumentMatrix(documentIndices, tokenIndices);
        tdm.setCounts(info.getCountsMatrix());

        List<Vector> centroids = info.getCentroids();

        KMeansClustering kmc = new KMeansClustering(NUM_OF_CLUSTERS, tdm.counts(), new Random());
        List<TIntList> clusters = kmc.clusters(centroids);
        for (TIntList cluster : clusters) {
            if (cluster.isEmpty()) {
                System.out.println("Empty cluster");
            }
            else {
                // Earliest doc in cluster
                System.out.println(documentIndicesInverted.get(kmc.earliestDoc(documentDates, cluster)));


                System.out.printf("--ratio %f--\n",ratio);
                TIntSet sharedTerms = tdm.partiallySharedTerms(cluster, ratio);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, tokenIndices, sharedTerms);
                }


                ratio = 0.5;
                System.out.printf("--ratio %f--\n",ratio);
                sharedTerms = tdm.partiallySharedTerms(cluster, ratio);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, tokenIndices, sharedTerms);
                }


                ratio = 0.7;
                System.out.printf("--ratio %f--\n",ratio);
                sharedTerms = tdm.partiallySharedTerms(cluster, ratio);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, tokenIndices, sharedTerms);
                }


                ratio = 1.0;
                System.out.printf("--ratio %f--\n",ratio);
                sharedTerms = tdm.partiallySharedTerms(cluster, ratio);

                System.out.println(cluster);
                for (Integer c : cluster.toArray()) {
                    tdm.nMostRelevantTerms(tdm.counts().getRow(c).toSparseVector(), 10, tokenIndices, sharedTerms);
                }
            }
            System.out.println();
        }
*/
    }
    
}
