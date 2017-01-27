package matrix;

import com.google.common.collect.BiMap;
import eu.danieldk.nlp.conllx.Token;
import gnu.trove.list.TIntList;
import io.Layer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.la4j.matrix.SparseMatrix;
import org.la4j.matrix.sparse.CRSMatrix;

/**
 *
 * @author Patricia Fischer
 */
public class TazMatrix implements TermDocumentMatrix {

    private final Layer layer;
    
    private final BiMap<String, Integer> documentIndices;
    
    private final BiMap<String, Integer> tokenIndices;
    
    private final Set<Integer> mostFrequent;
    
    private static SparseMatrix counts;
    
    
    public TazMatrix(Layer layer, BiMap<String, Integer> documentIndices,
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
     * @param articleContent
     * @throws IOException
     */
    public void processFile(String fileID, List<Token> articleContent) throws IOException {
        
        System.out.println(String.format("Add term frequencies of file %s to matrix", fileID));
        Integer fileIDIndex = documentIndices.get(fileID);
        if (fileIDIndex == null) {
            throw new IOException(String.format("Unknown file ID: %s", fileID));
        }

        for (Integer token : tokensToIndices(articleContent)) {
            if (!mostFrequent.contains(token)){
                counts.set(fileIDIndex, token, counts.get(fileIDIndex, token)+1);
            }
        }
        System.out.println(String.format("Done adding term frequencies of file %s to matrix", fileID));
    }

    /**
     * 
     * @param sectionContent
     * @param tokenIndices
     * @return
     * @throws IOException 
     */
    private List<Integer> tokensToIndices(List<Token> tokens) throws IOException {
        List<Integer> indices = new ArrayList<>();

        for (Token token : tokens) {
            String value = layer == Layer.LEMMA ?
                    token.getLemma().or("_") :
                    token.getForm().or("_");

            Integer idx = tokenIndices.get(value);
            if (idx == null) {
                throw new IOException(String.format("Token not in vocabulary: %s", value));
            }

            indices.add(idx);
        }

        return indices;
    }
    
    /**
     * 
     */
    private void countsToTfIdf(List<TIntList> documentFrequencies) {
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
