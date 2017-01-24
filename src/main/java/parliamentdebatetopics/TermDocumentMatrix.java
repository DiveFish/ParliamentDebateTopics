package parliamentdebatetopics;

import com.google.common.collect.BiMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.la4j.Vector;
import org.la4j.vector.SparseVector;

/**
 *
 * @author Daniël de Kok and Patricia Fischer
 */
public class TermDocumentMatrix {

    private final Layer layer;
    
    private final BiMap<String, Integer> documentIndices;
    private final BiMap<String, Integer> tokenIndices;
    private final Set<Integer> mostFrequent;
    
    private static Vector docVec;
    
    
    public TermDocumentMatrix(Layer layer, BiMap<String, Integer> documentIndices, BiMap<String, Integer> tokenIndices, Set<Integer> mostFrequent) {
        this.layer = layer;
        this.documentIndices = documentIndices;
        this.tokenIndices = tokenIndices;
        this.mostFrequent = mostFrequent;
    }
    
    /**
     *
     * @return
     */
    public Vector docVec() {
        return docVec;
    }
    
    /**
     *
     * @param documentFrequencies
     *
    public void tfIdf(Map<Integer, TIntList> documentFrequencies) {
        countsToTfIdf(documentFrequencies);
    }*/
    
    /**
     *
     * @param fileID
     * @param debate
     * @throws IOException
     */
    public void processDebate(String fileID, List<PolMineReader.DebateSection> debate) throws IOException {
        
        System.out.println(String.format("Create term-frequencies vector of file %s...", fileID));
        Integer fileIDIndex = documentIndices.get(fileID);
        if (fileIDIndex == null) {
            throw new IOException(String.format("Unknown file ID: %s", fileID));
        }
        
        docVec = SparseVector.zero(tokenIndices.size());
        for (PolMineReader.DebateSection section : debate){
            for (Integer token : tokensToIndices(section.contributionContent())) {
                if (!mostFrequent.contains(token)){
                    docVec.set(token, docVec.get(token)+1);
                }
            }
        }
        System.out.println("Term-frequencies vector created");
    }

    /**
     * 
     * @param sectionContent
     * @param tokenIndices
     * @return
     * @throws IOException 
     */
    private List<Integer> tokensToIndices(List<String> sectionContent) throws IOException {
        
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
     *
    private void countsToTfIdf(Map<Integer, TIntList> documentFrequencies) {
        for (int i = 0; i < counts.rows(); i++) {
            for(int j = 0; j < counts.columns(); j++){
                if (!(documentFrequencies.get(j) == null)){
                    counts.set(i, j, counts.get(i,j) *
                        Math.log(counts.rows()/documentFrequencies.get(j).size()));
                }
            }
        }
    }*/
}
