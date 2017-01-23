package parliamentdebatetopics;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import gnu.trove.list.TIntList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.la4j.matrix.SparseMatrix;
import org.la4j.matrix.sparse.CCSMatrix;

/**
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class TermDocumentMatrix {

    private final Layer layer;
    
    private static SparseMatrix counts;
    
    
    public TermDocumentMatrix(Layer layer, Integer numOfDocs, Integer maxVocabSize) {
        this.layer = layer;
        counts = CCSMatrix.zero(numOfDocs, maxVocabSize);
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
     * @param documentFrequencies
     */
    public void tfIdf(Map<Integer, TIntList> documentFrequencies) {
        countsToTfIdf(documentFrequencies);
    }
    
    /**
     *
     * @param fromCol
     */
    public void removeEmptyRows(Integer fromCol){
        int[] selRows = Ints.toArray(ContiguousSet.create(Range.closed(0, counts.rows()-1), DiscreteDomain.integers()));
        int[] selCols = Ints.toArray(ContiguousSet.create(Range.closed(0, fromCol), DiscreteDomain.integers()));
        counts = (SparseMatrix) counts.select(selRows, selCols);
    }
        
    /**
     *
     * @param mostFrequent
     */
    public void removeMostFrequent(Set<Integer> mostFrequent){
        for(int i : mostFrequent){
            counts.setColumn(i,0);
        }
    }
    
    /**
     *
     * @param fileID
     * @param debate
     * @param documentIndices
     * @param tokenIndices
     * @throws IOException
     */
    public void processDebate(String fileID, List<PolmineReader.DebateSection> debate, Map<String, Integer> documentIndices, Map<String, Integer> tokenIndices) throws IOException {
        
        Integer fileIDIndex = documentIndices.get(fileID);
        if (fileIDIndex == null) {
            throw new IOException(String.format("Unknown file ID: %s", fileID));
        }

        for (PolmineReader.DebateSection section : debate){
            for (Integer token : tokensToIndices(section.contributionContent(), tokenIndices)) {
                counts.set(fileIDIndex, token, counts.get(fileIDIndex, token)+1);
            }
        }
    }

    /**
     * 
     * @param sectionContent
     * @param tokenIndices
     * @return
     * @throws IOException 
     */
    private List<Integer> tokensToIndices(List<String> sectionContent, Map<String, Integer> tokenIndices) throws IOException {
        
        List<Integer> indices = new ArrayList<>();

        switch(layer)
        {
            case LEMMA:
                //TODO: add lemma/token behaviour differentiation
                break;
            case TOKEN:
                //TODO: add lemma/token behaviour differentiation
                break;
            default:
                System.out.println("Provide level of extraction, choose between \"TOKEN\" and \"LEMMA\"");
        }

        for (String token : sectionContent) {
            Integer idx = tokenIndices.get(token);
            if (idx == null) {
                throw new IOException(String.format("Token not in vocabulary: %s", token));
            }
            indices.add(idx);
        }

        return indices;
    }
    
    /**
     * 
     */
    private void countsToTfIdf(Map<Integer, TIntList> documentFrequencies) {
        for (int i = 0; i < counts.rows(); i++) {
            for(int j = 0; j < counts.columns(); j++){
                if (!(documentFrequencies.get(j) == null)){
                    counts.set(i, j, counts.get(i,j) *
                        Math.log(counts.rows()/documentFrequencies.get(j).size()));
                }
            }
        }
    }
}
