package io;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.Layer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.PolMineReader.DebateSection;

/**
 * Build a vocabulary from text input. Associate each word (lemma or token)
 * and each document with an index.
 * 
 * The document frequency keeps track of all the documents in which a term
 * occurs so that these counts do not have to be calculated separately for the
 * tf.idf matrix.
 * The token counts keep track of the overall frequency of a token in the whole
 * corpus and can be used to retrieve (and filter) the most frequent tokens.
 * These can be considered stopwords.
 * 
 * @author Daniël de Kok and Patricia Fischer
 */
public class VocabularyPolMine implements Vocabulary {

    private final Layer layer;
    
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
    
    public VocabularyPolMine(Layer layer) {
        this.layer = layer;
        documentIndices = HashBiMap.create();
        tokenIndices = HashBiMap.create();
        tokenCounts = new TIntArrayList();
        documentFrequencies = new ArrayList<>();
    }

    /**
     * Process content of each debate section and save file ID.
     * 
     * @param fileID The file name of the debate
     * @param debate The debate sections and their respective metadata
     * @throws IOException
     */
    public void processFile(String fileID, List<DebateSection> debate) throws IOException {
        System.out.println("Processing debate vocabulary...");
        extractFileID(fileID);
        for(DebateSection section : debate) {
            extractVocabulary(documentIndices.get(fileID), section.contributionContent());
        }
        System.out.println(String.format("Done with vocab in file %s", fileID));
        
    }
    
    /**
     * Extract tokens from text and assign them an index.
     * Process them either as lemmas or tokens.
     * 
     * @param sectionContent The content of a debate section
     */
    private void extractVocabulary(Integer fileIDIndex, List<String> sectionContent) throws IOException {
        switch (layer) {
            case TOKEN:
                //TODO: add lemma/token behaviour differentiation
                break;
            case LEMMA:
                //TODO: add lemma/token behaviour differentiation
                break;
            default:
                System.out.println("Provide level of extraction, choose between \"TOKEN\" and \"LEMMA\"");
                break;
        }

        for (String token : sectionContent) {
            Integer index = tokenIndices.get(token);
            if (index == null) {
                index = tokenIndices.size();
                tokenIndices.put(token, index);
                tokenCounts.add(1);  // add count 1 for this token at end of list (position will equal token index)
                
            }
            else {
                tokenCounts.set(index, tokenCounts.get(index) + 1);
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
     * Add file ID to documentIndices, assigning each ID an index.
     * 
     * @param debateID The file ID of the debate
     */
    private void extractFileID(String debateID) {
        if (documentIndices == null) {
            return;
        }
        documentIndices.putIfAbsent(debateID, documentIndices.size());
    }

    /**
     *
     * @return The file IDs and their associated indices
     */
    @Override
    public BiMap<String, Integer> documentIndices() {
        return documentIndices;
    }

    /**
     *
     * @return The tokens and their associated indices
     */
    @Override
    public BiMap<String, Integer> tokenIndices() {
        return tokenIndices;
    }
    
    /**
     *
     * @return The list of token counts
     */
    @Override
    public TIntList tokenCounts() {
        return tokenCounts;
    }

    /**
     *
     * @return A list of document index lists
     */
    @Override
    public List<TIntList> documentFrequencies() {
        return documentFrequencies;
    }
    
}