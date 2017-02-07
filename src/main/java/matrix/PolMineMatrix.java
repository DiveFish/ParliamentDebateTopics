package matrix;

import com.google.common.collect.BiMap;
import gnu.trove.list.TIntList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.la4j.matrix.SparseMatrix;
import org.la4j.matrix.sparse.CRSMatrix;
import io.Layer;
import io.PolMineReader;

/**
 * 
 * @author Daniël de Kok and Patricia Fischer
 */
public class PolMineMatrix implements TermDocumentMatrix {

    private final Layer layer;
    
    private final BiMap<String, Integer> documentIndices;
    
    private final BiMap<String, Integer> tokenIndices;
    
    private final Set<Integer> mostFrequent;
    
    private static SparseMatrix counts;
    
    
    public PolMineMatrix(Layer layer, BiMap<String, Integer> documentIndices,
            BiMap<String, Integer> tokenIndices, Set<Integer> mostFrequent) {
        this.layer = layer;
        this.documentIndices = documentIndices;
        this.tokenIndices = tokenIndices;
        this.mostFrequent = mostFrequent;
        counts = CRSMatrix.zero(documentIndices.size(), tokenIndices.size());
    }
    
    /**
     *
     * @return
     */
    @Override
    public SparseMatrix counts() {
        return counts;
    }
    
    /**
     *
     * @param documentFrequencies
     */
    @Override
    public void tfIdf(List<TIntList> documentFrequencies) {
        countsToTfIdf(documentFrequencies);
    }
    
    /**
     *
     * @param fileID
     * @param debate
     * @throws IOException
     */
    public void processFile(String fileID, List<PolMineReader.DebateSection> debate) throws IOException {
        
        System.out.println(String.format("Add term frequencies from file %s", fileID));
        Integer fileIDIndex = documentIndices.get(fileID);
        if (fileIDIndex == null) {
            throw new IOException(String.format("Unknown file ID: %s", fileID));
        }

        for (PolMineReader.DebateSection section : debate){
            for (Integer token : tokensToIndices(section.contributionContent())) {
                if (!mostFrequent.contains(token)){
                    counts.set(fileIDIndex, token, counts.get(fileIDIndex, token)+1);
                }
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
    private List<Integer> tokensToIndices(List<String> sectionContent) throws IOException {
        
        List<Integer> indices = new ArrayList<>(sectionContent.size());

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
     * @param documentFrequencies The document frequencies of all terms
     */
    private void countsToTfIdf(List<TIntList> documentFrequencies) {
        int numOfDocuments = counts.rows();
        for (int i = 0; i < numOfDocuments; i++) {
            for(int j = 0; j < counts.columns(); j++){
                if (!(documentFrequencies.get(j) == null)){
                    counts.set(i, j, counts.get(i,j) * Math.log(numOfDocuments/documentFrequencies.get(j).size()));
                }
            }
            System.out.println(i+1+" files done");
        }
    }
}
