package compact;

import com.carrotsearch.hppc.BitSet;
import com.google.common.base.Preconditions;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import org.la4j.Matrix;
import org.la4j.matrix.SparseMatrix;
import org.la4j.vector.SparseVector;

import java.io.*;
import java.util.*;

/**
 * Utility class for finding the best number of bits for the document hashes.
 *
 * @author Patricia Fischer
 */
public class HashUtils {

    public static void main(String[] args) throws FileNotFoundException {
        File dir = new File("/home/patricia/Dokumente/Bachelorarbeit/Results/PolMine/binary/hammingDistances_scaled");
        hammingDistanceToSimilarity(dir);
    }

    /**
     * Convert hamming distance to similarity for all files containing the hamming distances.
     *
     * @param directory
     * @throws FileNotFoundException
     */
    public static void hammingDistanceToSimilarity(File directory) throws FileNotFoundException {
        for (File file: directory.listFiles()) {
            List<Double> hammingSimilarities = new ArrayList();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine(); // skip heading
                while ((line = br.readLine()) != null) {
                    double hammingSimilarity = convertHammingDistance(Double.parseDouble(line.trim()));
                    hammingSimilarities.add(hammingSimilarity);
                }

                String fileName = "/home/patricia/Dokumente/Bachelorarbeit/Results/PolMine/binary/hammingDistances_scaled/"+file.getName().substring(0,file.getName().length()-4);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName+"_scaled.txt"))));
                writer.write("hamming_similarity\n");
                for (Double hammSim : hammingSimilarities) {
                    writer.write(Double.toString(hammSim)+"\n");
                }

                writer.close();
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }


    /**
     * Hamming distance values are subtracted from 1 to obtain hamming similarity.
     *
     * @param hammingDistance The hamming distance to be scaled and converted
     * @return The scaled hamming similarity
     */
    private static double convertHammingDistance(Double hammingDistance) {
        return 1.0-hammingDistance;
    }

    /**
     * Calculate for m documents the cosine similarity to all other documents, pick the n most
     * similar documents and also calculate the hamming similarity for them. Compare the cosine
     * and hamming similarity by their correlation and return the average of all correlations.
     *
     * ! Put numbers into thesis
     *
     * @param counts The tf-idf matrix
     * @param documentHashes The documents as bit vectors
     * @return The average correlation between cosine and hamming similarity
     */
    public static double getBestHammingDistance(SparseMatrix counts, List<BitSet> documentHashes) {
        //1. Take a document d.
        //2. Find the n documents that are the most similar to d according to cosine similarity.
        //3. Compute (1) the cosine similarities of each document and d, (2) the hamming similarity of the hashed representations of each document and d. Storing both pairwise per document.
        //4. Goto 1 until you have processed a reasonable number of documents.
        TDoubleList averageCorrelation = new TDoubleArrayList();
        normalizeDocumentVectors(counts);
        for (int doc = 0; doc < counts.rows(); doc++) {
            TDoubleList cosineSimilaritiesAll = new TDoubleArrayList();
            for (int row = 0; row < counts.rows(); row++) {
                //System.out.println("Cos: "+counts.getRow(doc).innerProduct(counts.getRow(row)));
                cosineSimilaritiesAll.add(counts.getRow(doc).innerProduct(counts.getRow(row)));
            }

            Set<Integer> mostSimilar = mostSimilarDocuments(cosineSimilaritiesAll, 10); // get 15 most documents which are the most similar to current doc

            TDoubleList cosineSimilarities = new TDoubleArrayList();
            TDoubleList hammingSimilarities = new TDoubleArrayList();

            for (int i : mostSimilar) {
                BitSet hammingBits = (BitSet) documentHashes.get(i).clone();
                hammingBits.xor(documentHashes.get(doc));
                cosineSimilarities.add(cosineSimilaritiesAll.get(i));
                hammingSimilarities.add(1-((double) hammingBits.cardinality()/(double) hammingBits.size())); // number of bits set to true/ Hamming distance
            }
            double corr = getCorrelation(cosineSimilarities, hammingSimilarities);
            if (corr > 0) {
                averageCorrelation.add(corr);
            }
        }

        double averageCorr = averageCorrelation.sum()/averageCorrelation.size();
        System.out.printf("Average correlation between cosine and hamming similarity: %s\n", averageCorr);

        return averageCorr;
    }

    private static double norm(SparseVector v) {
        // Euclidean norm in la4j uses BigDecimal, which has a lot of overhead.
        return Math.sqrt(v.innerProduct(v));
    }

    private static void normalizeDocumentVectors(Matrix documentVectors) {
        for (int i = 0; i < documentVectors.rows(); i++) {
            SparseVector doc = documentVectors.getRow(i).toSparseVector();
            documentVectors.setRow(i, doc.divide(norm(doc)));
        }
    }

    /**
     * Calculate the correlation between cosine and hamming similarities.
     *
     * Adapted from: https://stackoverflow.com/questions/28428365/how-to-find-correlation-between-two-integer-arrays-in-java
     * (accessed 08 June 2017)
     *
     * @param cosineSimilarities A list of cosine similarities
     * @param hammingSimilarities A list of hamming similarities
     * @return The correlation between the two lists of doubles
     */
    public static double getCorrelation(TDoubleList cosineSimilarities, TDoubleList hammingSimilarities) {

        Preconditions.checkNotNull(cosineSimilarities, String.format("CosineSimilarities is null."));
        Preconditions.checkNotNull(hammingSimilarities, String.format("HammingSimilarities is null."));
        Preconditions.checkArgument(cosineSimilarities.size() == hammingSimilarities.size(),
                String.format("List should be of same size, but are of sizes %d and %d", cosineSimilarities.size(), hammingSimilarities.size()));

        double sumX = cosineSimilarities.sum();
        if (sumX == 0) {
            return -1;
        }
        double sumY = hammingSimilarities.sum();
        double sumXX = 0.0;
        double sumYY = 0.0;
        double sumXY = 0.0;

        int n = cosineSimilarities.size();

        for(int i = 0; i < n; ++i) {
            double x = cosineSimilarities.get(i);
            double y = hammingSimilarities.get(i);

            sumXX += x * x;
            sumYY += y * y;
            sumXY += x * y;
        }

        // Co-variation, standard error of x and y
        double cov = sumXY / n - sumX * sumY / n / n;
        double sigmaX = Math.sqrt(sumXX / n -  sumX * sumX / n / n);
        double sigmaY = Math.sqrt(sumYY / n -  sumY * sumY / n / n);

        // Correlation = normalized co-variation
        return cov / sigmaX / sigmaY;
    }

    /**
     * Find the n highest similarity scores and return their document indices.
     *
     * @param cosineSimilarities A list of cosine similarity scores
     * @param n The number of most similar documents
     * @return The n most similar documents
     */
    protected static Set<Integer> mostSimilarDocuments(TDoubleList cosineSimilarities, int n) {
        ScoreComparator comparator = new ScoreComparator(cosineSimilarities);
        BestN<Integer> bestN = new BestN(n, comparator);

        for (int i = 0; i < cosineSimilarities.size(); i++) {
            bestN.add(i);
        }

        return new HashSet(bestN);
    }

    /**
     * Comparator for documents (by index). A document with a higher cosine similarity is sorted
     * before a document with a lower cosine similarity.
     */
    private static class ScoreComparator implements Comparator<Integer> {
        private final TDoubleList scores;

        private ScoreComparator(TDoubleList scores) {
            this.scores = scores;
        }

        @Override
        public int compare(Integer idx1, Integer idx2) {
            int scoreCmp = Double.compare(scores.get(idx2), scores.get(idx1));
            if (scoreCmp != 0)
                return scoreCmp;

            return Integer.compare(idx1, idx2);
        }
    }
}
