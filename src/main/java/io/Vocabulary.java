package io;

import com.google.common.collect.BiMap;
import gnu.trove.list.TIntList;
import java.util.List;

/**
 *
 * @author Patricia Fischer
 */
public interface Vocabulary {
    
    /*
     * TODO: uncomment when PolMine has been transformed into CONLL format
     */
    //public void processFile(String fileID, List<Token> fileContent);
            
    /*
     * The documents and their associated indices
     */    
    public BiMap<String, Integer> documentIndices();

    /*
     * The tokens and their associated indices
     */
    public BiMap<String, Integer> tokenIndices();
    
    /* 
     * The list of token counts
     */
    public TIntList tokenCounts();

    /*
     * A list of document index lists, providing the terms' document frequencies
     */
    public List<TIntList> documentFrequencies();
}
