package io;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import eu.danieldk.nlp.conllx.Token;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniël de Kok and Patricia Fischer
 */
public class VocabularyTaz implements Vocabulary {
    
    private final Layer layer;

    private final BiMap<String, Integer> documentIndices;

    private final BiMap<String, Integer> tokenIndices;

    private final TIntList tokenCounts;

    private final List<TIntList> documentFrequencies; //stores for each term the documents in which it occurs
    
    public VocabularyTaz(Layer layer) throws IOException {
        this.layer = layer;
        this.documentIndices = HashBiMap.create();
        tokenIndices = HashBiMap.create();
        tokenCounts = new TIntArrayList();
        documentFrequencies = new ArrayList<>();
    }

    public void processFile(String articleID, List<Token> articleContent) {
        System.out.println("Processing article vocabulary...");
        extractArticleID(articleID);
        extractVocabulary(documentIndices.get(articleID), articleContent);
        System.out.println(String.format("Done with vocab in file %s", articleID));

    }

    private void extractVocabulary(Integer articleIndex, List<Token> articleContent) {
        for (Token token : articleContent) {
            String value = layer == Layer.LEMMA ?
                    token.getLemma().or("_") :
                    token.getForm().or("_");
            Integer index = tokenIndices.get(value);
            if (index == null) {
                index = tokenIndices.size();
                tokenIndices.put(value, index);
                tokenCounts.add(1);
            } else {
                tokenCounts.set(index, tokenCounts.get(index) + 1);
            }
            
            //Add all document indices to document index list of respective
            //token to obtain token's document frequency
            if (documentFrequencies.size() == index) {
                documentFrequencies.add(new TIntArrayList());
            }

            TIntList documents = documentFrequencies.get(index);
            
            // Use binary search for O(log N) search.
            int idx = documents.binarySearch(articleIndex);
            if (idx < 0) { // fileIDIndex not yet in document list of this token
                idx = ~idx;
                documents.insert(idx, articleIndex);
            }
        }
    }

    private void extractArticleID(String articleID) {
        if (documentIndices == null) {
            return;
        }

        documentIndices.putIfAbsent(articleID, documentIndices.size());
    }

    @Override
    public BiMap<String, Integer> documentIndices() {
        return documentIndices;
    }

    @Override
    public BiMap<String, Integer> tokenIndices() {
        return tokenIndices;
    }
    
    @Override
    public TIntList tokenCounts() {
        return tokenCounts;
    }
    
    @Override
    public List<TIntList> documentFrequencies() {
        return documentFrequencies;
    }
    
}
