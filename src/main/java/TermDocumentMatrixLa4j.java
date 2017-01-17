package parliamentdebatetopics;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.annolab.tt4j.TreeTaggerException;
import org.la4j.matrix.SparseMatrix;
import org.la4j.matrix.sparse.CCSMatrix;
import parliamentdebatetopics.PolmineReader.DebateSection;

/**
 * Create a term-document and tf.idf matrix from the Polmine debates. All debates
 * and the vocabulary extracted from the debates are associated with indices
 * that can be used to construct the term-document matrix. A term's document
 * frequency is kept track of in a separate map to make retrieval of the numbers
 * faster.
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class TermDocumentMatrixLa4j {
    private final String layer;

    private final Map<String, Integer> documentIndices;

    private final Map<String, Integer> tokenIndices;
    
    private final Map<Integer, TIntList> documentFrequencies;

    private final Set<Integer> mostFrequent;

    private final SparseMatrix counts;
    
    private final SparseMatrix tfIdf;

    /**
     *
     * @param layer
     * @param documentIndices
     * @param tokenIndices
     * @param mostFrequent
     * @throws IOException
     */
    public TermDocumentMatrixLa4j(String layer, Map<String, Integer> documentIndices, Map<String, Integer> tokenIndices,
                                     Set<Integer> mostFrequent) throws IOException {
        this.layer = layer;
        this.documentIndices = documentIndices;
        this.tokenIndices = tokenIndices;
        this.mostFrequent = mostFrequent;
        documentFrequencies = new HashMap<>();
        counts = CCSMatrix.zero(documentIndices.size(), tokenIndices.size());
        tfIdf = CCSMatrix.zero(documentIndices.size(), tokenIndices.size());
    }

    /**
     *
     * @return
     */
    public SparseMatrix counts() {
        return counts;
    }
    
    /**
     *
     * @param matrix
     * @return
     */
    public SparseMatrix tfIdf(SparseMatrix matrix) {
        countsToTfIdf(counts);
        return tfIdf;
    }

    /**
     *
     * @param debates
     * @throws IOException
     * @throws TreeTaggerException
     */
    public void processDebates(HashMap<String, List<DebateSection>> debates) throws IOException, TreeTaggerException{
        for (Map.Entry<String, List<DebateSection>> debate : debates.entrySet())
        {
            Integer fileID = documentIndices.get(debate.getKey());
            if (fileID == null) {
                throw new IOException("Unknown file ID: " + debate.getKey());
            }
            
            for (DebateSection section : debate.getValue()){
                for (Integer token : tokensToIndices(section.contributionContent())) {
                    if (!mostFrequent.contains(token)) {
                        counts.set(fileID, token, counts.get(fileID, token)+1);
                        
                        if (!documentFrequencies.containsKey(token)) {
                            documentFrequencies.putIfAbsent(token, new TIntArrayList());
                        }
                        if (!documentFrequencies.get(token).contains(fileID)){
                            documentFrequencies.get(token).add(fileID);
                        }
                    }
                }
            }
        }
        /*// to check content of documentFrequencies HashMap
        for (Map.Entry<Integer, TIntList> docFreq : documentFrequencies.entrySet()){
            System.out.println(docFreq);
        }
        */
    }

    private List<Integer> tokensToIndices(List<String> sectionContent) throws IOException, TreeTaggerException {
        List<Integer> indices = new ArrayList<>();

        if (layer.equals("lemma")){
            Lemmas lems = new Lemmas();        
            sectionContent = lems.lemmatizeDebateSection(sectionContent);
        }
        else if (!layer.equals("form"))
            System.out.println("Provide level of extraction, choose between \"lemma\" and \"form\"");

        for (String token : sectionContent) {
            Integer idx = tokenIndices.get(token);
            if (idx == null) {
                throw new IOException(String.format("Token not in vocabulary: %s", token));
            }
            indices.add(idx);
            //System.out.println("\""+token+"\" index: "+idx);  // to check which word is assigned which index
        }

        return indices;
    }
    
    /**
     * 
     * @param matrix The term-document matrix to be transformed into a tf.idf matrix
     */
    private void countsToTfIdf(SparseMatrix matrix) {
        int numDocs = matrix.rows();
        for (int i = 0; i < numDocs; i++) {
            for(int j = 0; j < matrix.columns(); j++){
                if (!(documentFrequencies.get(j)==null)){
                    if (documentFrequencies.get(j).size()>0){
                        tfIdf.set(i, j, matrix.get(i,j)*
                            Math.log(numDocs/documentFrequencies.get(j).size()));

                    }
                }
            }
        }
    }
}
