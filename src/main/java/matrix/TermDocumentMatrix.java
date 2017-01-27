package matrix;

import gnu.trove.list.TIntList;
import java.util.List;
import org.la4j.matrix.SparseMatrix;

/**
 *
 * @author Patricia Fischer
 */
public interface TermDocumentMatrix {
    
    /*
     * TODO: uncomment when PolMine has been transformed into CONLL format
     */
    //public void processFile(String fileID, List<Token> fileContent)
    
    /*
     *
     */
    public SparseMatrix counts();
    
    /*
     *
     */
    public void tfIdf(List<TIntList> documentFrequencies);
}
