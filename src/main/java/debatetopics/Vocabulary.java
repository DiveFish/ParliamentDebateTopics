package debatetopics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.IOException;
import java.util.List;

import debatetopics.PolmineReader.DebateSection;
import java.util.HashMap;
import java.util.Map;

/**
 * Build a vocabulary from text input. Associate each word (lemma or token)
 * and each document with an index.
 * 
 * @author Daniël de Kok and Patricia Fischer
 */
public class Vocabulary {

    private final BiMap<String, Integer> documentIndices;
    private final BiMap<String, Integer> tokenIndices;
    private final Map<Integer, TIntList> documentFrequencies;
    private final TIntList tokenCounts;  //for getting the most frequent tokens/ stopwords
    private final Layer layer;

    public Vocabulary() {
        documentIndices = HashBiMap.create();
        tokenIndices = HashBiMap.create();
        documentFrequencies = new HashMap();
        tokenCounts = new TIntArrayList();
        layer = null;
    }
    
    /**
     * Switch between LEMMA and TOKEN layer.
     * Layer LEMMA will lemmatize the input text and process word types.
     * Layer TOKEN will skip lemmatization and process the input by tokens.
     * @param layer The layer of word extraction
     */
    
    public Vocabulary(Layer layer) {
        documentIndices = HashBiMap.create();
        tokenIndices = HashBiMap.create();
        documentFrequencies = new HashMap();
        tokenCounts = new TIntArrayList();
        this.layer = layer;
    }

    /**
     * Step through debates, process content of each debate section and save 
     * file ID for each debate.
     * Process ALL debates to have access to the file ID.
     * @param fileID The file name of the debate
     * @param debate The debate sections and their respective metadata
     * @throws IOException
     */
    public void processDebate(String fileID, List<DebateSection> debate) throws IOException {
        System.out.println("Processing debate vocabulary...");
        extractFileID(fileID);
        for(DebateSection section : debate){
            extractVocabulary(section.contributionContent(), documentIndices.get(fileID));
        }
        System.out.println(String.format("Done with vocab in file %s", fileID));
        
    }
    
    /**
     * Extract tokens from text, process them either as lemmas or tokens.
     * Save them in a vocabulary map which also keeps track of the
     * count of lemmas or tokens.
     * @param sectionContent The content of a debate section
     */
    private void extractVocabulary(List<String> sectionContent, Integer fileIDIndex) throws IOException {
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
                tokenCounts.add(1);
            }
            else {
                tokenCounts.set(index, tokenCounts.get(index) + 1);
            }
            
            //Add all document indices to document index list of respective token to obtain token's document frequency
            if (!documentFrequencies.containsKey(index)) {
                documentFrequencies.putIfAbsent(index, new TIntArrayList());
            }
            if (!documentFrequencies.get(index).contains(fileIDIndex)){
                documentFrequencies.get(index).add(fileIDIndex);
            }
        }
    }

    /**
     * 
     * @param debateID The file name of the debate
     */
    private void extractFileID(String debateID) {
        if (documentIndices == null) {
            return;
        }
        documentIndices.putIfAbsent(debateID, documentIndices.size());
    }

    /**
     *
     * @return
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
     * @return
     */
    public Map<Integer, TIntList> documentFrequencies() {
        return documentFrequencies;
    }
    
    /**
     *
     * @return The list of token counts
     */
    public TIntList tokenCounts() {
        return tokenCounts;
    }

}