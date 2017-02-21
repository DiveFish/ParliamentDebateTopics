package compact;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Build a vocabulary from text input. Associate each word (lemma or token)
 * and each document with an index.
 * 
 * The document frequency keeps track of all the documents in which a term
 * occurs so that these counts do not have to be calculated separately for the
 * tf.idf matrix.
 * The token counts keep track of the overall frequency of a token in the whole
 * corpus and can be used to retrieve (and filter) the most frequent tokens
 * which are thereby considered stopwords.
 * 
 * 
 * @author Daniël de Kok and Patricia Fischer
 */
public class Vocabulary {
    
    private final BiMap<String, Integer> documentIndices;
    
    private final BiMap<String, Integer> tokenIndices;
    
    private final TIntList tokenCounts;  //for getting the most frequent tokens/ stopwords
    
    private final List<TIntList> documentFrequencies; //stores for each term the documents in which it occurs
    
    /**
     * Vocabulary can be created on lemmas or tokens.
     * Layer LEMMA will lemmatize the input text and process word types.
     * Layer TOKEN will skip lemmatization and process the input by tokens.
     * 
     * @param layer The layer of word extraction
     */
    
    public Vocabulary(Layer layer) {
        documentIndices = HashBiMap.create();
        tokenIndices = HashBiMap.create();
        tokenCounts = new TIntArrayList();
        documentFrequencies = new ArrayList<>();
    }
    
    // processFile()
    // extractFileID()
    // extractVocabulary()
    // <- input: List<Map<String, Integer>>; List<String> = wordFrequencies and file IDs
    
    /**
     * Process content of each debate section and save file ID.
     * 
     * @param sectionID The file name of the debate
     * @param wordFrequencies
     * @throws IOException
     */
    public void processSection(String sectionID, Map<String, Integer> wordFrequencies) throws IOException {
        extractSectionID(sectionID);
        extractVocabulary(documentIndices.get(sectionID), wordFrequencies);
    }
    
    /**
     * Extract tokens from text and assign them an index.
     * Process them either as lemmas or tokens.
     * 
     * @param wordFrequencies The content represented as words and their respective frequency
     */
    private void extractVocabulary(Integer fileIDIndex, Map<String, Integer> wordFrequencies) throws IOException {
        for (Map.Entry<String, Integer> entry : wordFrequencies.entrySet()) {
            String token = entry.getKey();
            Integer frequency = entry.getValue();
            Integer index = tokenIndices.get(token);
            if (index == null) {
                index = tokenIndices.size();
                tokenIndices.put(token, index);
                tokenCounts.add(1);  // add count 1 for this token at end of list (position will equal token index)
                
            }
            else {
                tokenCounts.set(index, tokenCounts.get(index) + frequency);
            }

            //Add all document indices to document index list of respective
            //token to obtain token's document frequency
            if (documentFrequencies.size() == index) {
                documentFrequencies.add(new TIntArrayList());
            }

            TIntList documents = documentFrequencies.get(index);
            // Use binary search for O(log N) search.
            int idx = documents.binarySearch(fileIDIndex);
            if (idx < 0) { // fileIDIndex not yet in document list of this token
                idx = ~idx;
                documents.insert(idx, fileIDIndex);
            }
        }
    }
    
    /**
     * Add section ID to documentIndices, assigning each ID an index.
     * 
     * @param sectionID The file ID of the debate
     */
    private void extractSectionID(String sectionID) {
        if (documentIndices == null) {
            return;
        }
        documentIndices.putIfAbsent(sectionID, documentIndices.size());
    }

    /**
     *
     * @return The file IDs and their associated indices
     */
    public BiMap<String, Integer> documentIndices() {
        return documentIndices;
    }

    /**
     *
     * @return The tokens and their associated indices
     */
    public BiMap<String, Integer> tokenIndices() {
        return tokenIndices;
    }
    
    /**
     *
     * @return The list of token counts
     */
    public TIntList tokenCounts() {
        return tokenCounts;
    }

    /**
     *
     * @return A list of document index lists
     */
    public List<TIntList> documentFrequencies() {
        return documentFrequencies;
    }   
}
