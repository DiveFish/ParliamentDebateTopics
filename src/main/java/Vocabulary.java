package parliamentdebatetopics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.annolab.tt4j.TreeTaggerException;

import parliamentdebatetopics.PolmineReader.DebateSection;

/**
 * Build a vocabulary from text input. Associate each word (lemma or word form)
 * and each document with an index.
 * 
 * @author Daniël de Kok and Patricia Fischer
 */
public class Vocabulary {

    private final BiMap<String, Integer> documentIndices;
    private final BiMap<String, Integer> tokenIndices;
    private final TIntList tokenCounts;
    private final String layer;

    public Vocabulary() {
        documentIndices = HashBiMap.create();
        tokenIndices = HashBiMap.create();
        tokenCounts = new TIntArrayList();
        layer = "lemma";  // change to "form" if you want to process tokens, not types
    }
    
    /*
     * Layer "lemma" will lemmatize the input text and process word types.
     * Layer "form" will skip lemmatization and process the input by tokens.
    */
    public Vocabulary(String layer) {
        documentIndices = HashBiMap.create();
        tokenIndices = HashBiMap.create();
        tokenCounts = new TIntArrayList();
        this.layer = layer;
    }

    public BiMap<String, Integer> documentIndices() {
        return documentIndices;
    }

    /*
     * Step through debates, process content of each debate section and save 
     * file ID for each debate.
     * Process ALL debates to have access to the file ID.
        
     * TODOs:
     * - Should we save the fileID with each debate section?
     *   or give a single debateSection as input, not all debates?
    */
    public void processDebates(HashMap<String, List<DebateSection>> debates) throws IOException, TreeTaggerException{
        System.out.println("Processing debate vocabulary...");
        for (Map.Entry<String, List<DebateSection>> debate : debates.entrySet())
        {
            for(DebateSection section : debate.getValue())
                extractVocabulary(section.contributionContent(), layer);
            extractFileID(debate.getKey());
            System.out.println("Done with vocab in file "+debate.getKey());
        }
    }
    
    /*
        Extract tokens from text, process them either as lemmas or as unchanged
        word form. Save them in a vocabulary map which also keeps track of the
        word count (lemma: types, word form: tokens).
    */
    private void extractVocabulary(List<String> sectionContent, String layer) throws IOException, TreeTaggerException {
        switch (layer) {
            case "lemma":
                Lemmas lems = new Lemmas();
                sectionContent = lems.lemmatizeDebateSection(sectionContent);
                break;
            case "form":
                break;
            default:
                System.out.println("Provide level of extraction, choose between \"lemma\" and \"form\"");
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
        }
    }

    private void extractFileID(String debateID) {
        if (documentIndices == null) {
            return;
        }
        documentIndices.putIfAbsent(debateID, documentIndices.size());
    }

    public TIntList tokenCounts() {
        return tokenCounts;
    }

    public BiMap<String, Integer> tokenIndices() {
        return tokenIndices;
    }
}